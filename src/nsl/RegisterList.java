/*
 * RegisterList.java
 */

package nsl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Manages the use of NSIS's built in registers ($0-$9, $R0-$R9) as well as user defined registers
 * (Var).
 *
 * @author Stuart
 */
public class RegisterList {
	private final ArrayList<Register> registerList;
	private final HashMap<String, Register> registerMap;

	private static RegisterList current = new RegisterList();

	/** Class constructor. */
	public RegisterList() {
		this.registerList = new ArrayList<Register>();
		this.registerMap = new HashMap<String, Register>();
		this.add("$0", RegisterType.Register, false);
		this.add("$1", RegisterType.Register, false);
		this.add("$2", RegisterType.Register, false);
		this.add("$3", RegisterType.Register, false);
		this.add("$4", RegisterType.Register, false);
		this.add("$5", RegisterType.Register, false);
		this.add("$6", RegisterType.Register, false);
		this.add("$7", RegisterType.Register, false);
		this.add("$8", RegisterType.Register, false);
		this.add("$9", RegisterType.Register, false);
		this.add("$R0", RegisterType.Register, false);
		this.add("$R1", RegisterType.Register, false);
		this.add("$R2", RegisterType.Register, false);
		this.add("$R3", RegisterType.Register, false);
		this.add("$R4", RegisterType.Register, false);
		this.add("$R5", RegisterType.Register, false);
		this.add("$R6", RegisterType.Register, false);
		this.add("$R7", RegisterType.Register, false);
		this.add("$R8", RegisterType.Register, false);
		this.add("$R9", RegisterType.Register, false);
		this.add("$INSTDIR", RegisterType.Other, true);
		this.add("$OUTDIR", RegisterType.Other, true);
	}

	/**
	 * Adds a register to the list.
	 *
	 * @param name the register name
	 * @param registerType the type of register
	 * @param inUse whether or not the current register is in use
	 */
	private void add(String name, RegisterType registerType, boolean inUse) {
		Register register = new Register(name, this.registerList.size(), registerType, inUse);
		this.registerList.add(register);
		this.registerMap.put(register.toString(), register);

		if (registerType == RegisterType.Other) {
			Scope.getGlobal().addVar(register.getIndex());
			Scope.getGlobalUninstaller().addVar(register.getIndex());
		}
	}

	/**
	 * Adds a user defined register (variable).
	 *
	 * @param name the register name
	 * @return the register index
	 */
	public int add(String name) {
		Register register = this.registerMap.get(name);
		if (register != null) {
			register.setInUse(true);
			return register.getIndex();
		}

		int index = this.registerList.size();
		register = new Register(name, index, RegisterType.Variable, false);
		register.setInUse(true);
		this.registerList.add(register);
		this.registerMap.put(register.toString(), register);
		return index;
	}

	/**
	 * Gets a register by index number.
	 *
	 * @param index the register index
	 * @return the register
	 */
	public Register get(int index) {
		return this.registerList.get(index);
	}

	/**
	 * Sets the {@code Register.setInUse} value for all built in registers.
	 *
	 * @param inUse the new value
	 */
	public void setAllInUse(boolean inUse) {
		for (Register register : this.registerList) {
			if (register.getRegisterType() == RegisterType.Register) register.setInUse(inUse);
			else break;
		}
	}

	/**
	 * Gets the next freely available NSIS register.
	 *
	 * @return the next freely available NSIS register
	 */
	public Register getNext() {
		for (Register register : this.registerList) {
			if (register.getRegisterType() != RegisterType.Other && !register.getInUse()) {
				register.setInUse(true);
				return register;
			}
		}

		throw new NslException(
				"Out of registers and variables (used all " + this.registerList.size() + ")!");
	}

	/**
	 * Gets the current register list.
	 *
	 * @return the current register list
	 */
	public static RegisterList getCurrent() {
		return current;
	}

	/**
	 * Sets the current register list.
	 *
	 * @param current the current register list
	 */
	public static void setCurrent(RegisterList current) {
		RegisterList.current = current;
	}

	/** Writes the variable declarations to the output stream. */
	public void defineVars() throws IOException {
		for (Register register : this.registerList)
			if (register.getRegisterType() == RegisterType.Variable)
				ScriptParser.writeLine("Var " + register.toString().substring(1));
	}
}
