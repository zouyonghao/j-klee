package com.j.klee.core.impl;

import com.j.klee.core.*;
import com.j.klee.core.mem.Heap;
import com.j.klee.core.mem.MemoryManager;
import com.j.klee.core.mem.MemoryObject;
import com.j.klee.expr.Expr;
import com.j.klee.expr.impl.BoolBinaryExpr.BoolAndExpr;
import com.j.klee.expr.impl.BoolExpr;
import com.j.klee.expr.impl.BoolNotExpr;
import com.j.klee.expr.impl.CmpExpr.EqExpr;
import com.j.klee.expr.impl.ConstantExpr;
import com.j.klee.expr.impl.ZExtExpr;
import com.j.klee.module.Cell;
import com.j.klee.module.KFunction;
import com.j.klee.module.KInstruction;
import com.j.klee.module.KModule;
import com.j.klee.solver.Solver;
import com.j.klee.utils.DataLayout;
import com.j.klee.utils.LLVMUtils;
import com.j.klee.utils.LLVMUtils.Intrinsic;
import org.bytedeco.llvm.LLVM.LLVMBasicBlockRef;
import org.bytedeco.llvm.LLVM.LLVMModuleRef;
import org.bytedeco.llvm.LLVM.LLVMTypeRef;
import org.bytedeco.llvm.LLVM.LLVMValueRef;
import org.bytedeco.llvm.global.LLVM;

import java.util.*;

import static com.j.klee.core.impl.ExecutorUtil.evalConstant;
import static com.j.klee.expr.Expr.Width.getWidthFromInt;
import static org.bytedeco.llvm.global.LLVM.*;

public class ExecutorImpl implements Executor {

    public enum MemOpType {MemOpRead, MemOpWrite, MemOpName, MemOpNop}

    public enum MemOpResultType {MemOpSuccess, MemOpOOB, MemOpError}

    public static class MemOpResult {
        public MemOpResultType resultType;
        public MemoryObject mo;
    }

    private KModule kModule;
    private SortedSet<ExecutionState> states = new TreeSet<>();
    private boolean haltExecution = false;
    private MemoryManager memoryManager = new MemoryManager();
    private SpecialFunctionHandler specialFunctionHandler;
    private Solver solver;

    @Override
    public void runFunctionAsMain(LLVMValueRef f, int argc, char[][] argv, char[][] envp) {
        // TODO: argvMO
        // TODO: arguments

        KFunction kf = kModule.getFunctionMap().get(f);
        assert (kf != null);
        // assert (getFunctionArgumentSize(f) == 0);

        ExecutionState state = new ExecutionState(kModule.getFunctionMap().get(f));

        // TODO: bind arguments
        // TODO: initialize global variables

        // TODO: process tree
        run(state);
    }

    @Override
    public void run(ExecutionState initialState) {
        bindModuleConstants();

        // TODO: timers
        states.add(initialState);

        // TODO: using seed

        // TODO: searcher

        while (!states.isEmpty() && !haltExecution) {
            ExecutionState state = states.first();
            state.prevPC.setTo(state.pc.index);
            KInstruction ki = state.pc.getKInst();

            // TODO: step instruction
            state.pc.next();

            executeInstruction(state, ki);

            // TODO: update states
            updateStates(state);

            // TODO: dump states
            // TODO: check memory usage
        }
    }

    private void updateStates(ExecutionState state) {
        if (!state.pc.hasNext()) {
            states.remove(state);
        }
    }

    private void executeInstruction(ExecutionState state, KInstruction ki) {
        LLVMValueRef inst = ki.getInst();
        LLVMUtils.dumpValue(inst);
        switch (LLVMGetInstructionOpcode(inst)) {
            case LLVMRet -> {
                System.out.println("currently unsupported inst: ret");
                // TODO: ret
                terminateState(state);
            }
            case LLVMBr -> {
                if (LLVMUtils.isUnconditional(inst)) {
                    LLVMValueRef bb = LLVM.LLVMGetOperand(inst, 0);
                    assert (LLVM.LLVMValueIsBasicBlock(bb) == LLVMUtils.True);
                    transferToBasicBlock(LLVMValueAsBasicBlock(bb), LLVMGetInstructionParent(inst), state);
                } else {
                    Expr cond = eval(ki, 0, state).value;

                    // TODO: optimizer

                    StatePair statePair = fork(state, cond, false, BranchType.ConditionalBranch);

                    // TODO: track coverage

                    LLVMBasicBlockRef parent = LLVMGetInstructionParent(inst);
                    if (statePair.trueState != null) {
                        LLVMBasicBlockRef trueBB = LLVM.LLVMGetSuccessor(inst, 0);
                        transferToBasicBlock(trueBB, parent, statePair.trueState);
                    }
                    if (statePair.falseState != null) {
                        LLVMBasicBlockRef falseBB = LLVM.LLVMGetSuccessor(inst, 1);
                        transferToBasicBlock(falseBB, parent, statePair.falseState);
                    }

                    // TODO: fork reason
                }
            }
            case LLVMIndirectBr -> {
                Expr address = eval(ki, 0, state).value;

                if (address instanceof ConstantExpr ce) {
                    LLVMValueRef v = LLVMUtils.getValueFromAddress(ce.getZExtValue());
                    LLVMBasicBlockRef bb = LLVMValueAsBasicBlock(v);
                    assert (bb != null);
                    transferToBasicBlock(bb, LLVMGetInstructionParent(inst), state);
                } else {
                    // indirectbr i8* %23, [label %14, label %18, label %16]
                    int numOperands = LLVMGetNumOperands(inst);
                    List<LLVMBasicBlockRef> targets = new ArrayList<>();
                    List<Expr> expressions = new ArrayList<>();
                    Expr errorCase = new BoolExpr(true);

                    List<LLVMBasicBlockRef> destinations = new ArrayList<>();
                    for (int j = 1; j < numOperands; j++) {
                        LLVMBasicBlockRef dest = LLVMValueAsBasicBlock(LLVMGetOperand(inst, j));
                        if (destinations.contains(dest)) {
                            continue;
                        }
                        destinations.add(dest);

                        // TODO: is this correct? This line is different from Klee.
                        Expr destAddress = ConstantExpr.createPointer(dest.address(), address.getWidth());
                        Expr e = new EqExpr(address, destAddress);

                        errorCase = new BoolAndExpr(errorCase, new BoolNotExpr(e));

                        boolean result = solver.mayBeTrue(state.constraints, e);
                        if (result) {
                            targets.add(dest);
                            expressions.add(e);
                        }
                    }

                    for (int j = 0; j < targets.size(); j++) {
                        ExecutionState es = state.branch();
                        states.add(es);
                        addConstraint(es, expressions.get(j));
                        transferToBasicBlock(targets.get(j), LLVMGetInstructionParent(inst), es);
                    }

                    // check errorCase feasibility
                    boolean result = solver.mayBeTrue(state.constraints, errorCase);
                    if (result) {
                        throw new IllegalStateException("indirect br: illegal label address");
                    }
                    terminateState(state);
                }
            }
            case LLVMSwitch -> {
                // switch i32 %6, label %10 [
                //         i32 1, label %7
                //         i32 2, label %8
                // ]
                // Num of operands = 6
                // %6 (cond), %10 (default), 1, %7, 2, %8
                Expr cond = eval(ki, 0, state).value;
                LLVMBasicBlockRef bb = LLVMGetInstructionParent(inst);
                if (cond instanceof ConstantExpr) {
                    long longValue = ((ConstantExpr) cond).getZExtValue();
                    LLVMBasicBlockRef caseBB = LLVMUtils.findSwitchInstSuccessorByConstant(ki.getInst(), longValue);
                    transferToBasicBlock(caseBB, LLVMGetInstructionParent(ki.getInst()), state);
                } else {
                    // Handle possible different branch targets

                    // We have the following assumptions:
                    // - each case value is mutual exclusive to all other values
                    // - order of case branches is based on the order of the expressions of
                    //   the case values, still default is handled last
                    // List<LLVMBasicBlockRef> bbOrder = new ArrayList<>(); // TODO: do we need this?
                    Map<LLVMBasicBlockRef, Expr> branchTargets = new HashMap<>();
                    Map<Expr, LLVMBasicBlockRef> expressionOrder = new HashMap<>();

                    // Iterate through all non-default cases and order them by expressions
                    int numOperands = LLVMGetNumOperands(inst);
                    assert (numOperands > 3);
                    for (int i = 2; i < numOperands; i += 2) {
                        Expr value = evalConstant(LLVMGetOperand(inst, i));
                        LLVMBasicBlockRef caseSuccessor = LLVMValueAsBasicBlock(LLVMGetOperand(inst, i + 1));
                        expressionOrder.put(value, caseSuccessor);
                    }
                    // System.out.println(expressionOrder.size());

                    // Track default branch values
                    Expr defaultValue = new BoolExpr(true);
                    LLVMBasicBlockRef defaultDest = LLVMGetSwitchDefaultDest(inst);

                    // Iterate through all non-default cases but in order of the expressions
                    for (Map.Entry<Expr, LLVMBasicBlockRef> caseEntry : expressionOrder.entrySet()) {
                        // skip if case has same successor basic block as default case
                        // (should work even with phi nodes as a switch is a single terminating instruction)
                        if (caseEntry.getValue().address() == defaultDest.address()) {
                            continue;
                        }

                        Expr match = EqExpr.create(cond, caseEntry.getKey());

                        // Make sure that the default value does not contain this target's value
                        defaultValue = BoolAndExpr.create(defaultValue, new BoolNotExpr(match));
                        // TODO: optimizer
                        boolean result = solver.mayBeTrue(state.constraints, match);
                        if (result) {
                            LLVMBasicBlockRef caseSuccessor = caseEntry.getValue();
                            branchTargets.put(caseSuccessor, match);
                            System.out.println("case is possible");
                            caseEntry.getKey().print();
                            System.out.println();
                            // TODO: Handle the case that a basic block might be the target of multiple switch cases?
                        }
                    }

                    // Check if control could take the default case
                    boolean result = solver.mayBeTrue(state.constraints, defaultValue);
                    if (result) {
                        // branchTargets.put(defaultDest, defaultValue);
                        transferToBasicBlock(defaultDest, bb, state);
                    } else {
                        System.out.println("default value is not possible, so we terminate current state");
                        terminateState(state);
                    }

                    // Fork the current state with each state having one of the possible successors of this switch
                    for (Map.Entry<LLVMBasicBlockRef, Expr> caseEntry : branchTargets.entrySet()) {
                        ExecutionState es = state.branch();
                        states.add(es);
                        addConstraint(es, caseEntry.getValue());
                        transferToBasicBlock(caseEntry.getKey(), bb, es);
                    }
                }
            }
            case LLVMUnreachable -> {
                // TODO: terminate state
                System.out.println("run to unreachable code, end state " + state.id);
                terminateState(state);
            }
            case LLVMInvoke, LLVMCall -> {
                if (LLVM.LLVMIsADbgInfoIntrinsic(inst) != null) {
                    System.out.println("ignoring debug intrinsic call.");
                    break;
                }
                LLVMValueRef f = LLVM.LLVMGetCalledValue(inst);

                // TODO: Compute the true target of a function call, resolving LLVM aliases and bit casts

                if (LLVM.LLVMIsAInlineAsm(f) != null) {
                    System.out.println("run to inline asm, end state " + state.id);
                    terminateState(state);
                }

                List<Expr> arguments = new ArrayList<>();
                int numArgs = LLVMUtils.getFunctionArgumentSize(f);
                // CallInst and InvokeInst operands are stored
                // in a fixed order: function, arg0, arg1, ...
                // @see KFunction
                for (int j = 0; j < numArgs; j++) {
                    arguments.add(eval(ki, j + 1, state).value);
                }

                // TODO: special case the call with a bit cast
                executeCall(state, ki, f, arguments);
            }
            case LLVMPHI -> {
                Expr result = eval(ki, state.incomingBBIndex, state).value;
                result.print();
                bindLocal(ki, state, result);
                // TODO: loop invariant
            }
            case LLVMSelect -> {
                System.out.println("currently unsupported inst: select");
            }
            case LLVMVAArg -> {
                System.out.println("currently unsupported inst: vaarg");
            }
            case LLVMAdd -> {
                System.out.println("currently unsupported inst: add");
            }
            case LLVMSub -> {
                System.out.println("currently unsupported inst: sub");
            }
            case LLVMMul -> {
                System.out.println("currently unsupported inst: mul");
            }
            case LLVMUDiv -> {
                System.out.println("currently unsupported inst: udiv");
            }
            case LLVMSDiv -> {
                System.out.println("currently unsupported inst: sdiv");
            }
            case LLVMURem -> {
                System.out.println("currently unsupported inst: urem");
            }
            case LLVMSRem -> {
                System.out.println("currently unsupported inst: srem");
            }
            case LLVMAnd -> {
                System.out.println("currently unsupported inst: and");
            }
            case LLVMOr -> {
                System.out.println("currently unsupported inst: or");
            }
            case LLVMXor -> {
                System.out.println("currently unsupported inst: xor");
            }
            case LLVMShl -> {
                System.out.println("currently unsupported inst: shl");
            }
            case LLVMLShr -> {
                System.out.println("currently unsupported inst: lshr");
            }
            case LLVMAShr -> {
                System.out.println("currently unsupported inst: ashr");
            }
            case LLVMICmp -> {
                switch (LLVM.LLVMGetICmpPredicate(inst)) {
                    case LLVM.LLVMIntEQ -> {
                        Expr left = eval(ki, 0, state).value;
                        Expr right = eval(ki, 1, state).value;
                        Expr result = EqExpr.create(left, right);
                        result.print();
                        bindLocal(ki, state, result);
                    }
                    case LLVMIntNE -> {

                    }
                    default ->
                            System.out.println("currently unsupported icmp predicate: " + LLVM.LLVMGetICmpPredicate(inst));
                }
            }
            /*
             * NOTE:
             * For alloca instruction, we will allocate a memory object with an
             * address, and we bind the address value to the target register.
             * Later, we use the address to find the memory object.
             */
            case LLVMAlloca -> {
                System.out.println("executing alloca..." + state.pc.getKInst().getInfo().getLine());
                LLVMTypeRef allocType = LLVMGetAllocatedType(inst);
                long byteSize = this.kModule.getTargetData().LLVMStoreSizeOfType(allocType);
                Expr size = Expr.createPointer(byteSize);
                // TODO: array allocation
                executeAlloc(state, size, true, ki);
            }
            case LLVMLoad -> {
                System.out.println("executing load..." + state.pc.getKInst().getInfo().getLine());
                Expr baseAddr = eval(ki, 0, state).value;
                executeMemoryOperation(state, MemOpType.MemOpRead, baseAddr, null, ki, Expr.Width.InvalidWidth, "");
            }
            case LLVMStore -> {
                System.out.println("executing store..." + state.pc.getKInst().getInfo().getLine());
                Expr baseAddr = eval(ki, 1, state).value;
                Expr value = eval(ki, 0, state).value;
                executeMemoryOperation(state, MemOpType.MemOpWrite, baseAddr, value, null, Expr.Width.InvalidWidth, "");
            }
            case LLVMGetElementPtr -> {
                System.out.println("currently unsupported inst: getElementPtr");
            }
            case LLVMTrunc -> {
                System.out.println("currently unsupported inst: trunc");
            }
            case LLVMZExt -> {
                Expr result = ZExtExpr.create(eval(ki, 0, state).value, getWidthForLLVMType(LLVM.LLVMTypeOf(inst)));
                result.print();
                bindLocal(ki, state, result);
            }
            case LLVMSExt -> {
                System.out.println("currently unsupported inst: sext");
            }
            case LLVMIntToPtr -> {
                System.out.println("currently unsupported inst: intToPtr");
            }
            case LLVMPtrToInt -> {
                System.out.println("currently unsupported inst: ptrToInt");
            }
            case LLVMBitCast -> {
                System.out.println("currently unsupported inst: bitCast");
            }
            case LLVMFNeg, LLVMFAdd, LLVMFSub, LLVMFMul, LLVMFDiv, LLVMFRem, LLVMFPTrunc, LLVMFPExt, LLVMFPToUI,
                 LLVMFPToSI, LLVMUIToFP, LLVMSIToFP, LLVMFCmp -> {
                System.out.println("currently unsupported inst: floating point");
            }
            case LLVMInsertValue -> {
                System.out.println("currently unsupported inst: insert value");
            }
            case LLVMExtractValue -> {
                System.out.println("currently unsupported inst: extract value");
            }
            case LLVMFence -> {
                System.out.println("currently unsupported inst: fence");
            }
            case LLVMInsertElement -> {
                System.out.println("currently unsupported inst: insert element");
            }
            case LLVMExtractElement -> {
                System.out.println("currently unsupported inst: extract element");
            }
            case LLVMShuffleVector -> {
                System.out.println("currently unsupported inst: shuffle vector");
            }
            case LLVMResume -> {
                System.out.println("currently unsupported inst: resume");
            }
            case LLVMLandingPad -> {
                System.out.println("currently unsupported inst: landing pad");
            }
            case LLVMAtomicRMW -> {
                System.out.println("currently unsupported inst: rmw");
            }
            case LLVMAtomicCmpXchg -> {
                System.out.println("currently unsupported inst: xchg");
            }
            default -> {
                System.out.println("currently unsupported inst: " + LLVMGetInstructionOpcode(inst));
            }
        }
    }

    private void executeCall(ExecutionState state, KInstruction ki, LLVMValueRef f, List<Expr> arguments) {
        if (f != null && LLVM.LLVMIsDeclaration(f) == LLVMUtils.True) {
            System.out.println("function intrinsic id is " + LLVM.LLVMGetIntrinsicID(f));
            switch (LLVM.LLVMGetIntrinsicID(f)) {
                case Intrinsic.NotIntrinsic -> {
                    callExternalFunction(state, ki, f, arguments);
                }
                case Intrinsic.FAbs -> {
                    throw new IllegalStateException("not supported FAbs intrinsic function");
                }
                default -> {
                    throw new IllegalStateException("not supported intrinsic function: " + LLVM.LLVMGetIntrinsicID(f));
                }

                // TODO: invoke inst
            }
        }
    }

    private void callExternalFunction(ExecutionState state, KInstruction target, LLVMValueRef f, List<Expr> arguments) {
        if (specialFunctionHandler.handle(state, f, target, arguments)) {
            return;
        }
        throw new IllegalStateException("no support for calling external functions");
    }

    public void terminateState(ExecutionState state) {
        states.remove(state);
    }

    @Override
    public void initializeSolver() {
        solver = new Solver();
        // TODO: solver chain
    }

    private StatePair fork(ExecutionState current, Expr condition, boolean isInternal, int reason) {
        // TODO: check branch feasible
        Solver.Validity evaluateResult = solver.evaluate(current.constraints, condition
                // TODO: query meta data
                // current.queryMetaData
        );

        if (evaluateResult == Solver.Validity.True) {
            return new StatePair(current, null);
        } else if (evaluateResult == Solver.Validity.False) {
            return new StatePair(null, current);
        } else {
            // use current as true state
            ExecutionState falseState = current.branch();
            // TODO: added states
            // TODO: process tree

            states.add(falseState);

            addConstraint(current, condition);
            addConstraint(falseState, Expr.createIsZero(condition));

            return new StatePair(current, falseState);
        }
    }

    private void addConstraint(ExecutionState state, Expr condition) {
        // TODO: check invalid constraint

        state.addConstraint(condition);
    }

    private void transferToBasicBlock(LLVMBasicBlockRef dst, LLVMBasicBlockRef src, ExecutionState state) {
        KFunction kf = state.stack.getLast().kf;
        int entry = kf.getBasicBlockEntry().get(dst);
        state.pc.setTo(entry);
        System.out.println("transfer to index: " + entry);
        if (LLVM.LLVMGetInstructionOpcode(state.pc.getInst()) == LLVMPHI) {
            state.incomingBBIndex = LLVMUtils.getBasicBlockIndex(state.pc.getInst(), src);
        }
    }

    public void executeMemoryOperation(ExecutionState state, MemOpType memOpType, Expr address, Expr value, KInstruction target, Expr.Width objSize, String name) {
        // TODO: execute memory operation
        Expr.Width type;
        if (memOpType == MemOpType.MemOpWrite) {
            type = value.getWidth();
        } else if (memOpType == MemOpType.MemOpRead) {
            type = getWidthForLLVMType(LLVM.LLVMTypeOf(target.getInst()));
        } else if (memOpType == MemOpType.MemOpName) {
            type = objSize;
        } else {
            throw new IllegalStateException("Unknown memop type: " + memOpType);
        }

        int bytes = getMinBytesForWidth(type);
        System.out.println("mem op bytes = " + bytes);

        // TODO: forall

        MemOpResult result = trySingleResolution(state, memOpType, address, value, target, objSize, name,
                /* heap */ state.heap, bytes, type,
                /* useHeapConstraints */ true,
                /* useFZoneConstraints */ false,
                /* fZoneIndex */ -1);
        if (result.resultType == MemOpResultType.MemOpSuccess) {
            return;
        }
        throw new IllegalStateException("Could not resolve address: " + address);
    }

    private MemOpResult trySingleResolution(ExecutionState state, MemOpType memOpType, Expr address, Expr value,
            /**/ KInstruction target, Expr.Width objSize, String name,
            /**/ Heap heap, int bytes, Expr.Width type,
            /**/ boolean useHeapConstraints,
            /**/ boolean useFZoneConstraints,
            /**/int fZoneIndex) {
        MemOpResult result = heap.resolveOneExact(state, address);
        // TODO: bounds checking
        Expr offset = result.mo.getOffsetExpr(address);
        executeInBoundsMemOp(state, state.heap, memOpType, result.mo, offset, address, value, target, name, type);
        return result;
    }

    private void executeInBoundsMemOp(ExecutionState state, Heap heap, MemOpType memOpType, MemoryObject mo, Expr offset, Expr address, Expr value, KInstruction target, String name, Expr.Width type) {
        switch (memOpType) {
            case MemOpWrite -> {
                // TODO: old values?
                // TODO: read only?
                // TODO: forall
                mo.write(offset, value);
            }
            case MemOpRead -> {
                Expr result = mo.read(offset, type, /* ignoreWrites */ !mo.isLocal);
                result.print();
                System.out.println();

                // TODO: forall

                bindLocal(target, state, result);
            }
            case MemOpName -> {

            }
            default -> {
                throw new IllegalStateException("Unknown mem op type: " + memOpType);
            }
        }
    }

    private int getMinBytesForWidth(Expr.Width w) {
        return (w.getWidth() + 7) / 8;
    }

    private Expr.Width getWidthForLLVMType(LLVMTypeRef type) {
        return getWidthFromInt(kModule.getTargetData().getTypeSizeInBits(type));
    }

    public Cell eval(KInstruction ki, int index, ExecutionState state) {
        int valueNumber = ki.getOperands()[index];

        if (valueNumber < 0) {
            int constantIndex = -valueNumber - 2;
            return kModule.constantTable[constantIndex];
        } else {
            StackFrame sf = state.stack.getLast();
            return sf.locals[valueNumber];
        }
    }

    public void executeAlloc(ExecutionState state, Expr size, boolean isLocal, KInstruction ki) {
        executeAlloc(state, size, isLocal, ki, false, null, 0);
    }

    public void executeAlloc(ExecutionState state, Expr size, boolean isLocal, KInstruction ki, boolean zeroMemory, Object reAllocFrom, int allocationAlignment) {
        // TODO: toUnique size
        // TODO: optimize size
        MemoryObject mo = memoryManager.allocate(size, isLocal, false, state.prevPC.getInst(), allocationAlignment);

        // TODO: object state
        bindObjectInState(state, mo, isLocal);
        // TODO: initialize to zero/random
        bindLocal(ki, state, mo.address);
    }

    private void bindLocal(KInstruction target, ExecutionState state, Expr value) {
        getDestCell(state, target).value = value;
    }

    private Cell getDestCell(ExecutionState state, KInstruction target) {
        return state.stack.getLast().locals[target.getDest()];
    }

    private void bindObjectInState(ExecutionState state, MemoryObject mo, boolean isLocal) {

        state.heap.addObject(mo);

        if (isLocal) {
            state.stack.getLast().allocas.add(mo);
        }
    }

    private void bindModuleConstants() {
        for (KFunction kf : kModule.getFunctionMap().values()) {
            for (int i = 0; i < kf.getNumInstructions(); i++) {
                bindInstructionConstants(kf.getInstructions()[i]);
            }
        }

        kModule.constantTable = new Cell[kModule.constants.size()];
        for (int i = 0; i < kModule.constants.size(); i++) {
            Cell c = new Cell();
            c.value = evalConstant(kModule.constants.get(i));
            kModule.constantTable[i] = c;
        }
    }

    private void bindInstructionConstants(KInstruction instruction) {
        // TODO: GetElementPtrInst, InsertValueInst, ExtractValueInst
    }

    @Override
    public LLVMModuleRef setModule(List<LLVMModuleRef> modules, ModuleOptions moduleOptions) {
        assert (kModule == null);
        kModule = new KModule();
        // TODO: link with KLEE intrinsics library before running any optimizations
        // TODO: 1) link the modules together
        kModule.link(modules, moduleOptions);

        // TODO: 2) apply different instrumentation
        // TODO: 3) optimise and prepare for KLEE

        // TODO: preserve functions
        List<String> preservedFunctions = new ArrayList<>();
        specialFunctionHandler = new SpecialFunctionHandlerImpl(this);
        specialFunctionHandler.prepare(preservedFunctions);

        preservedFunctions.add(moduleOptions.entryPoint);
        preservedFunctions.add("memset");
        preservedFunctions.add("memcpy");
        preservedFunctions.add("memcmp");
        preservedFunctions.add("memmove");

        kModule.optimiseAndPrepare(moduleOptions, preservedFunctions);
        kModule.checkModule();

        // TODO: 4) Manifest the module
        kModule.manifest(
                // interpreter,
                // statsTracker
        );

        specialFunctionHandler.bind();

        // TODO: stats tracker

        // TODO: initialize the context

        DataLayout dataLayout = kModule.getTargetData();
        Context.initialize(dataLayout.isLittleEndian(), getWidthFromInt(dataLayout.getPointerSizeInBits()));

        return kModule.getModuleRef();
    }

    @Override
    public KModule getKModule() {
        return kModule;
    }

    public static class StatePair {
        public ExecutionState trueState;
        public ExecutionState falseState;

        public StatePair(ExecutionState trueState, ExecutionState falseState) {
            this.trueState = trueState;
            this.falseState = falseState;
        }
    }
}
