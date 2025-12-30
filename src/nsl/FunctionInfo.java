/*
 * FunctionInfo.java
 */

package nsl;

import java.util.ArrayList;

/**
 * Describes a function or function call.
 *
 * @author Stuart
 */
public class FunctionInfo extends CodeInfo {
	private static final ArrayList<FunctionInfo> list = new ArrayList<FunctionInfo>();

	private final String name;
	private final int params;
	private int returns;
	private final boolean isCallback;

	/**
	 * Class constructor specifying the function name and parameters.
	 *
	 * @param name the function name
	 * @param params the function parameters
	 */
	private FunctionInfo(String name, ArrayList<Register> params) {
		this.name = name;
		this.params = params.size();
		this.returns = -1;
		for (Register param : params)
			this.usedVars.put(Integer.valueOf(param.getIntegerValue()), param);
		this.isCallback = isCallbackFunction(name);
	}

	/**
	 * Creates the given function while adding it to the list of functions.
	 *
	 * @param name the function name
	 * @param params the function parameters
	 */
	public static FunctionInfo create(String name, ArrayList<Register> params) {
		FunctionInfo functionInfo = new FunctionInfo(name, params);
		list.add(functionInfo);
		return functionInfo;
	}

	/**
	 * Determines if the parser is in a function.
	 *
	 * @return <code>true</code> if the parser is in a function
	 */
	public static boolean in() {
		return current != null && current instanceof FunctionInfo;
	}

	/**
	 * Gets the current code info.
	 *
	 * @return the current code info
	 */
	public static FunctionInfo getCurrent() {
		return (FunctionInfo) current;
	}

	/**
	 * Determines if the given function is an NSIS built in callback function.
	 *
	 * @param name
	 * @return
	 */
	private static boolean isCallbackFunction(String name) {
		name = name.toLowerCase();
		return name.equals(".onguiinit")
				|| name.equals("un.onguiinit")
				|| name.equals(".oninit")
				|| name.equals("un.oninit")
				|| name.equals(".oninstfailed")
				|| name.equals("un.oninstfailed")
				|| name.equals(".oninstsuccess")
				|| name.equals("un.oninstsuccess")
				|| name.equals(".onguiend")
				|| name.equals("un.onguiend")
				|| name.equals(".onmouseoversection")
				|| name.equals(".onrebootfailed")
				|| name.equals("un.onrebootfailed")
				|| name.equals(".onselchange")
				|| name.equals("un.onselchange")
				|| name.equals(".onuserabort")
				|| name.equals("un.onuserabort")
				|| name.equals(".onverifyinstdir");
	}

	/**
	 * Gets the function name.
	 *
	 * @return the function name
	 */
	public String getName() {
		if (this.isCallback || this.returns == -1 || (this.params == 0 && this.returns == 0))
			return this.name;
		return this.name + '_' + this.params + '_' + this.returns;
	}

	/**
	 * Gets the function parameters.
	 *
	 * @return the function parameters
	 */
	public int getParams() {
		return this.params;
	}

	/**
	 * Gets the function return parameters.
	 *
	 * @return the function return parameters
	 */
	public int getReturns() {
		return this.returns;
	}

	/**
	 * Sets the function return parameters.
	 *
	 * @param returns the function return parameters
	 */
	public void setReturns(int returns) {
		this.returns = returns;
	}

	/**
	 * Gets the list of functions.
	 *
	 * @return the list of functions
	 */
	public static ArrayList<FunctionInfo> getList() {
		return list;
	}

	/**
	 * Determines if the .onInit and un.onInit functions are defined.
	 *
	 * @return 0 if neither are defined, 1 if .onInit is defined, 2 if un.onInit is defined or 3 if
	 *     both are defined
	 */
	public static int isOnInitDefined() {
		int ret = 0;
		for (FunctionInfo functionInfo : list) {
			if (functionInfo.name.equalsIgnoreCase(".onInit")) ret |= 1;
			else if (functionInfo.name.equalsIgnoreCase("un.onInit")) ret |= 2;
		}
		return ret;
	}

	/**
	 * Gets whether or not the function is an NSIS callback function
	 *
	 * @return whether or not the function is an NSIS callback function
	 */
	public boolean isCallback() {
		return this.isCallback;
	}

	/**
	 * Searches for the function that matches the given call.
	 *
	 * @param name the name of the function being called
	 * @param params the number of parameters
	 * @param returns the number of return items
	 * @return a {@link FunctionInfo} instance that matches the given call or <code>null</code>
	 *     otherwise.
	 */
	public static FunctionInfo find(String name, int params, int returns) {
		for (FunctionInfo functionInfo : list)
			if (functionInfo.matches(name, params, returns)) return functionInfo;
		return null;
	}

	/**
	 * Tests whether or not the current function call matches the given function call.
	 *
	 * @param name the name of the function being called
	 * @param params the number of parameters
	 * @param returns the number of return items
	 * @return <code>true</code> if the function calls match
	 */
	public boolean matches(String name, int params, int returns) {
		// Matching name?
		if (!this.name.equalsIgnoreCase(name)) return false;

		// Check call parameters count.
		if (this.params != params) return false;

		// Check return parameters count.
		if (returns > 0 && this.returns != returns) return false;

		/*// Check call parameters types.
		for (int i = 0; i < paramCount; i++)
			if (this.params.get(i).getType() != call.params.get(i).getType())
				return false;

		// Check return parameters types.
		for (int i = 0; i < returnCount; i++)
			if (this.returns.get(i).getType() != call.returns.get(i).getType())
				return false;*/

		return true;
	}
}
