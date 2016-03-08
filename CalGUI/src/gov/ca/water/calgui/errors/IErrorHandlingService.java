package gov.ca.water.calgui.errors;

public interface IErrorHandlingService {

	/**
	 * This is used when we want to set an unchecked exception handler for the whole program. Please use this only at the start of
	 * the program.
	 */
	public void setupGlobalExceptionHandling();

	/**
	 * This method is used to display the Validation related Errors. For example when user forget to enter the value in a field then
	 * we use this method to display the error.
	 *
	 * @param message
	 * @param detailMessage
	 */
	public void validationeErrorHandler(String displayMessage, String detailMessage);

	/**
	 * This method is used to display the Business related Errors. For example when we are doing some computation and if we get an
	 * error then we should use this method to display the erroror when the file is missing then we can use to tell the user.
	 *
	 * @param message
	 * @param detailMessage
	 */
	public void businessErrorHandler(String displayMessage, String detailMessage);
}
