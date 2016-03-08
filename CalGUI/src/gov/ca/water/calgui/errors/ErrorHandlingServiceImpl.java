package gov.ca.water.calgui.errors;

import java.awt.Dimension;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.apache.log4j.Logger;

import gov.ca.water.calgui.MainMenu;
import gov.ca.water.calgui.utils.Constant;

public class ErrorHandlingServiceImpl implements UncaughtExceptionHandler, IErrorHandlingService {

	private static Logger log = Logger.getLogger(ErrorHandlingServiceImpl.class.getName());

	@Override
	public void setupGlobalExceptionHandling() {
		Thread.setDefaultUncaughtExceptionHandler(new ErrorHandlingServiceImpl());
	}

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		displayErrorMessage("Uncaught Error : " + e.getMessage(), getStackTraceAsString(e));
	}

	@Override
	public void validationeErrorHandler(String displayMessage, String detailMessage) {
		displayErrorMessage("Validatione Error : " + displayMessage, detailMessage);
	}

	@Override
	public void businessErrorHandler(String displayMessage, String detailMessage) {
		displayErrorMessage("Business Error : " + displayMessage, detailMessage);
	}

	public void systemErrorHandler(String displayMessage, String detailMessage) {
		displayErrorMessage("Ssystem Error : " + displayMessage, detailMessage);
	}

	public String getStackTraceAsString(Throwable aThrowable) {
		Writer result = new StringWriter();
		PrintWriter printWriter = new PrintWriter(result);
		aThrowable.printStackTrace(printWriter);
		return result.toString();
	}

	private void displayErrorMessage(String displayMessage, String detailMessage) {
		Object[] options = { "ok", "show details" };
		int n = JOptionPane.showOptionDialog(MainMenu.desktop, displayMessage, "CalLite", JOptionPane.YES_NO_OPTION,
		        JOptionPane.INFORMATION_MESSAGE, null, options, options[1]);
		String emailMessage = "Display Message : " + displayMessage + "\n" + "Detail Message : " + detailMessage;
		sendEmail(emailMessage);
		JTextArea text = new JTextArea(detailMessage);
		JScrollPane scroll = new JScrollPane(text);
		scroll.setPreferredSize(new Dimension(600, 400));
		if (n == 1) {
			JOptionPane.showMessageDialog(MainMenu.desktop, scroll, "Error", JOptionPane.ERROR_MESSAGE);
		}
		log.error(emailMessage);
	}

	public void sendEmail(String message) {
		Properties props = new Properties();
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.smtp.starttls.enable", "true");

		Session session = Session.getDefaultInstance(props);

		try {
			InternetAddress fromAddress = new InternetAddress(Constant.FROM_ADDRESS);
			InternetAddress toAddress = new InternetAddress(Constant.TO_ADDRESS);

			Message mes = new MimeMessage(session);
			mes.setFrom(fromAddress);
			mes.setRecipient(Message.RecipientType.TO, toAddress);
			mes.setSubject(Constant.SUBJECT);
			mes.setText(message);
			Transport.send(mes, Constant.USER_NAME, new String(Constant.PASSWORD));
		} catch (MessagingException ex) {
			ex.printStackTrace();
		}
	}
}