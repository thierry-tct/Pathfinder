/*
 * Copyright (C) 2014, United States Government, as represented by the
 * Administrator of the National Aeronautics and Space Administration.
 * All rights reserved.
 *
 * The Java Pathfinder core (jpf-core) platform is licensed under the
 * Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0. 
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

// author corina pasareanu corina.pasareanu@sv.cmu.edu

package gov.nasa.jpf.symbc.bytecode;

import gov.nasa.jpf.symbc.SymbolicInstructionFactory;
import gov.nasa.jpf.symbc.arrays.ArrayExpression;
import gov.nasa.jpf.symbc.arrays.ObjectSymbolicArray;
import gov.nasa.jpf.symbc.arrays.SelectExpression;
import gov.nasa.jpf.symbc.heap.HeapChoiceGenerator;
import gov.nasa.jpf.symbc.heap.SymbolicInputHeap;
import gov.nasa.jpf.symbc.numeric.Comparator;
import gov.nasa.jpf.symbc.numeric.IntegerConstant;
import gov.nasa.jpf.symbc.numeric.IntegerExpression;
import gov.nasa.jpf.symbc.numeric.PCChoiceGenerator;
import gov.nasa.jpf.symbc.numeric.PathCondition;
import gov.nasa.jpf.vm.ArrayFields;
import gov.nasa.jpf.vm.ArrayIndexOutOfBoundsExecutiveException;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.Scheduler;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.ThreadInfo;

/**
 * Load reference from array
 * ..., arrayref, index  => ..., value
 */
public class AALOAD extends gov.nasa.jpf.jvm.bytecode.AALOAD {

	
  @Override
  public Instruction execute (ThreadInfo ti) {
	
      if (peekArrayAttr(ti) == null || !(peekArrayAttr(ti) instanceof ArrayExpression)) {
          // In this case, the array isn't symbolic
    	  if (peekIndexAttr(ti)==null || !(peekIndexAttr(ti) instanceof IntegerExpression)) {
              // In this case, the index isn't symbolic either
	    	  return super.execute(ti);
          }
      }
	  
      ObjectSymbolicArray arrayAttr = null;
      ChoiceGenerator<?> cg;
      boolean condition;

      if (!ti.isFirstStepInsn()) { // first time around
          cg = new PCChoiceGenerator(3);
          ((PCChoiceGenerator)cg).setOffset(this.position);
          ((PCChoiceGenerator)cg).setMethodName(this.getMethodInfo().getFullName());
          ti.getVM().setNextChoiceGenerator(cg);
          return this;
      } else { // this is what really returns results
        cg = ti.getVM().getChoiceGenerator();
        assert (cg instanceof PCChoiceGenerator) : "expected PCChoiceGenerator, got: " + cg;
      }

      PathCondition pc;
      ChoiceGenerator<?> prev_cg = cg.getPreviousChoiceGeneratorOfType(PCChoiceGenerator.class);

      if (prev_cg == null) {
          pc = new PathCondition();
      } else {
          pc = ((PCChoiceGenerator)prev_cg).getCurrentPC();
      }

      assert pc != null;

      if (peekArrayAttr(ti) == null || !(peekArrayAttr(ti) instanceof ArrayExpression)) {
          // In this case, the array isn't symbolic
          if (peekIndexAttr(ti) == null || !(peekIndexAttr(ti) instanceof IntegerExpression)) {
              // In this case, the index isn't symbolic either
              return super.execute(ti);
          }
          // We have a concrete array, but a symbolic index. We add all the constraints about the elements of the array and perform the select
          // TODO
          throw new RuntimeException("AALOAD : Concrete array, symbolic index not handled");
       } else {
           arrayAttr = (ObjectSymbolicArray)peekArrayAttr(ti);
       }

       IntegerExpression indexAttr = null;
       SelectExpression se = null;

   	   StackFrame frame = ti.getModifiableTopFrame();
	   arrayRef = frame.peek(1); // ..,arrayRef,idx

       if (peekIndexAttr(ti) == null || !(peekIndexAttr(ti) instanceof IntegerExpression)) {
           // In this case, the index isn't symbolic
           index = frame.peek();
           se = new SelectExpression(arrayAttr, index);
           indexAttr = new IntegerConstant(index);
       } else {
           indexAttr = (IntegerExpression)peekIndexAttr(ti);
           se = new SelectExpression(arrayAttr, indexAttr);
       }

       assert arrayAttr != null;
       assert indexAttr != null;
       assert se != null;

	    if (arrayRef == MJIEnv.NULL) {
	      return ti.createAndThrowException("java.lang.NullPointerException");
	    }
	  
        if ((Integer)cg.getNextChoice()==1) { // check bounds of the index
            pc._addDet(Comparator.GE, se.index, se.ae.length);
            if (pc.simplify()) { // satisfiable
                ((PCChoiceGenerator) cg).setCurrentPC(pc);
                return ti.createAndThrowException("java.lang.ArrayIndexOutOfBoundsException", "index greater than array bounds");
            } else {
                ti.getVM().getSystemState().setIgnored(true);
                return getNext(ti);
            }
        } else if ((Integer)cg.getNextChoice() == 2) {
            pc._addDet(Comparator.LT, se.index, new IntegerConstant(0));
            if (pc.simplify()) { // satisfiable
                ((PCChoiceGenerator) cg).setCurrentPC(pc);
                return ti.createAndThrowException("java.lang.ArrayIndexOutOfBoundsException", "index smaller than array bounds");
            } else {
                ti.getVM().getSystemState().setIgnored(true);
                return getNext(ti);
            }
        } else {
            pc._addDet(Comparator.LT, se.index, se.ae.length);
            pc._addDet(Comparator.GE, se.index, new IntegerConstant(0));
            if (pc.simplify()) { // satisfiable
                // TODO
                throw new RuntimeException("AALOAD : Symbolic array not handled");
            } else {
                ti.getVM().getSystemState().setIgnored(true);
                return getNext(ti);
            }
        }
  }
}
