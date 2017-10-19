package com.abdelama.mm7;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.Random;
import javax.activation.DataHandler;
import javax.xml.namespace.QName;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeader;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.util.Properties;

public class MM7Submit {

	private static SOAPMessage msg;
	static Properties prop;

	/** This is a sample web service operation */
	static SOAPConnectionFactory scfac = null;
	static SOAPConnection con = null;
	static MessageFactory fac = null;
	static SOAPMessage message = null;
	static SOAPMessage response = null;

	/**
	 * Inject MM7 header into SOAP message as required by MMSC.
	 * 
	 * @param message
	 * @param envelope
	 * @return A SOAPMessage with new header.
	 * @throws SOAPException
	 */
	public static SOAPMessage setMM7Header(SOAPMessage message, SOAPEnvelope envelope) throws SOAPException {

		// Random transaction ID
		Random randomNo = new Random();
		int pick = randomNo.nextInt(1234567890);

		// Http auth header
		String username = prop.getProperty("mmsc-username");
		String password = prop.getProperty("mmsc-password");
		String authorization = Base64Coder.encodeString(username + ":" + password);
		MimeHeaders mimeHeader = message.getMimeHeaders();

		// Add Auth to MIME Header
		mimeHeader.addHeader("Authorization", "Basic " + authorization);

		// Create and populate SOAP Header
		SOAPHeader soapHeader = message.getSOAPHeader();

		// IF no header at this point, add one, hmmm...weird
		if (soapHeader == null) {
			soapHeader = envelope.addHeader();
		}

		// Add these attributes to the envelope
		envelope.addAttribute(new QName("xsd"), "http://www.w3.org/2001/XMLSchema");
		envelope.addAttribute(new QName("xsi"), "http://www.w3.org/2001/XMLSchemainstance");

		// Add SOAP header element for MM7
		SOAPHeaderElement soapHeaderElement = soapHeader.addHeaderElement(envelope.createName("TransactionID", "mm7",
				"http://www.3gpp.org/ftp/Specs/archive/23_series/23.140/schema/REL-6-MM7-1-0"));

		// Set to "1" i.e MMSC must process this message
		soapHeaderElement.setMustUnderstand(true);

		// Add my random transaction id
		soapHeaderElement.setValue(Integer.toString(pick));

		// For Debug
		System.out.println("OG SOAP Headers>>");
		Iterator theMimeHdrIter = message.getMimeHeaders().getAllHeaders();
		for (; theMimeHdrIter.hasNext();) {
			MimeHeader theHdr = (MimeHeader) theMimeHdrIter.next();
			System.out.println("Header=" + theHdr.getName() + ",Value=" + theHdr.getValue());
		}
		System.out.println("End of OG Headers<<");
		// End Debug
		return msg;
	}

	/**
	 * Add text attachmentPart to SOAP message
	 * 
	 * @param message
	 * @return A SOAPMessage with text attachmentPart.
	 * @throws MalformedURLException
	 */
	public static SOAPMessage setText(SOAPMessage message, String txtMessage) throws MalformedURLException {

		AttachmentPart apText = message.createAttachmentPart();
		apText.setContent("text", "text/plain");
		apText.setContentType("text/html");
		apText.setContentId("whatever");
		message.addAttachmentPart(apText);
		// Debug
		System.out.println("Number of attachments = " + message.countAttachments());
		// End Debug
		return msg;
	}

	/**
	 * Add binary attachmentPart to SOAP message
	 * 
	 * @param message
	 *            file location to attach
	 * @return A SOAPMessage with binary attachmentPart.
	 * @throws MalformedURLException
	 */
	public static SOAPMessage setImage(SOAPMessage message, String imageLocation) throws MalformedURLException {

		DataHandler dataHandler = new DataHandler(new URL("file:///" + imageLocation));
		AttachmentPart apJpeg = message.createAttachmentPart(dataHandler);

		apJpeg.setContentId("An image for you");

		apJpeg.setContentType("image/gif");

		message.addAttachmentPart(apJpeg);

		// Debug
		System.out.println("Number of attachments = " + message.countAttachments());
		return msg;
	}

	/**
	 * Add binary attachmentPart to SOAP message
	 * 
	 * @param message
	 *            file location to attach
	 * @return A SOAPMessage with binary attachmentPart.
	 * @throws MalformedURLException
	 */
	public static SOAPMessage setVideo(SOAPMessage message, String videoLocation) throws MalformedURLException {

		DataHandler dataHandler = new DataHandler(new URL("file:///" + videoLocation));
		AttachmentPart apMpeg = message.createAttachmentPart(dataHandler);

		apMpeg.setContentId("A vid for you");
		apMpeg.setContentType("video/mpg");
		message.addAttachmentPart(apMpeg);

		// Debug
		System.out.println("Number of attachments = " + message.countAttachments());
		return msg;
	}

	/**
	 * Add binary attachmentPart to SOAP message
	 * 
	 * @param message
	 *            file location to attach
	 * @return A SOAPMessage with binary attachmentPart.
	 * @throws MalformedURLException
	 */
	public static SOAPMessage setAudio(SOAPMessage message, String audioLocation) throws MalformedURLException {

		DataHandler dataHandler = new DataHandler(new URL("file:///" + audioLocation));
		AttachmentPart apAudio = message.createAttachmentPart(dataHandler);

		apAudio.setContentId("A sound byte for you");

		apAudio.setContentType("audio/mp3");

		message.addAttachmentPart(apAudio);

		// Debug
		System.out.println("Number of attachments = " + message.countAttachments());
		return msg;
	}

	/**
	 * Add SOAPBody
	 * 
	 * @param message
	 * @param envelope
	 * @return A SOAPMessage with MM7 body added.
	 * @throws SOAPException
	 */
	public static SOAPMessage setMM7Body(SOAPMessage message, SOAPEnvelope envelope) throws SOAPException {

		SOAPBody bdy = envelope.getBody();
		Name submitReq = envelope.createName("SubmitReq", "mm7", "http://www.3gpp.org/ftp/Specs/archive/23_series/23.140/schema/REL-6-MM7-1-01");
		SOAPBodyElement submitReqElement = bdy.addBodyElement(submitReq);

		/*
		 * <MM7Version>5.6.0</MM7Version> <SenderIdentification>
		 * <VASPID>TNN</VASPID> <VASID>News</VASID> </SenderIdentification>
		 * <Recipients> <To> <Number>7255441234</Number> <RFC2822Address
		 * displayOnly="true">7255442222@OMMS.com</RFC2822Address> </To> <Cc>
		 * <Number>7255443333</Number> </Cc> <Bcc>
		 * <RFC2822Address>7255444444@OMMS.com</RFC2822Address> </Bcc>
		 * </Recipients> <ServiceCode>gold-sp33-im42</ServiceCode>
		 * <LinkedID>mms00016666</LinkedID>
		 * <MessageClass>Informational</MessageClass>
		 * <TimeStamp>2002-01-02T09:30:47-05:00</TimeStamp>
		 * <EarliestDeliveryTime
		 * >2002-01-02T09:30:47-05:00</EarliestDeliveryTime>
		 * <ExpiryDate>P90D</ExpiryDate> <DeliveryReport>true</DeliveryReport>
		 * <Priority>Normal</Priority> <Subject>News for today</Subject>
		 * <ContentClass>video-rich</ContentClass> <DRMContent>true</DRMContent>
		 * <ChargedParty>Sender</ChargedParty>
		 * <DistributionIndicator>true</DistributionIndicator> <Content
		 * href="cid:SaturnPics-01020930@news.tnn.com" allowAdaptations="true"/>
		 */

		// Add parameters
		submitReqElement.addChildElement("MM7Version").addTextNode("6.3.0");

		Name name = envelope.createName("SenderIdentification");
		SOAPElement senderID = submitReqElement.addChildElement(name);

		senderID.addChildElement("VASPID").addTextNode(prop.getProperty("vasp-id"));
		senderID.addChildElement("VASID").addTextNode(prop.getProperty("vas-id"));
		senderID.addChildElement("SenderAddress").addChildElement("Number").addTextNode(prop.getProperty("sender-address"));
		submitReqElement.addChildElement("Recipients").addChildElement("To").addChildElement("Number").addTextNode(prop.getProperty("recipient"));
		//submitReqElement.addChildElement("ServiceCode").addTextNode(prop.getProperty("service-code"));
		submitReqElement.addChildElement("AccessNumber").addTextNode(prop.getProperty("access-number"));

		//submitReqElement.addChildElement("ExpiryDate").addTextNode("P90D");
		//submitReqElement.addChildElement("ExpiryDate").addTextNode("2014-02-14T15:40:02Z");
		submitReqElement.addChildElement("DeliveryReport").addTextNode("true");
		submitReqElement.addChildElement("ReadReply").addTextNode("true");

		submitReqElement.addChildElement("Subject").addTextNode("SOLUTION ARCHITECTURE");

		//submitReqElement.addChildElement("ChargedParty").addTextNode(prop.getProperty("charged-party"));
		//submitReqElement.addChildElement("ChargedPartyID").addTextNode(prop.getProperty("charged-party-ID"));

		//submitReqElement.addChildElement("ApplicID").addTextNode("10.10.1.10:38192");
		//submitReqElement.addChildElement("ReplyApplicID").addTextNode("10.10.1.10:38192");
		submitReqElement.addChildElement("Content").setAttribute("allowAdaptations", "true");

		return msg;
	}

	public static String sender() {
		try {
			// Create Connection
			scfac = SOAPConnectionFactory.newInstance();
			con = scfac.createConnection();
			fac = MessageFactory.newInstance();
			message = fac.createMessage();

			// add <?xml version="1.0" encoding="UTF-8"?> to the message
			message.setProperty(SOAPMessage.WRITE_XML_DECLARATION, "true");

			SOAPPart soapPart = message.getSOAPPart();
			SOAPEnvelope envelope = soapPart.getEnvelope();

			SOAPHeader header = envelope.getHeader();
			// The SOAPHeader contains a header by default, remove this as it is
			// not needed
			// header.detachNode();

			setMM7Header(message, envelope);
			setMM7Body(message, envelope);
			File dir1 = new File(".");

			// Add all the attachments
			if (prop.getProperty("set-image").compareTo("true") == 0) {
				setImage(message, dir1.getCanonicalPath() + File.separator + "sample4.jpg");
				message.saveChanges();
			}

			if (prop.getProperty("set-text").compareTo("true") == 0) {
				setText(message, "A message from me");
				message.saveChanges();
			}

			if (prop.getProperty("set-audio").compareTo("true") == 0) {
				setAudio(message, dir1.getCanonicalPath() + File.separator + "sample6.mp3");
				message.saveChanges();
			}

			if (prop.getProperty("set-video").compareTo("true") == 0) {
				setVideo(message, dir1.getCanonicalPath() + File.separator + "sample5.mpg");
				message.saveChanges();
			}

			// why use ByteArrayOutputStream instead of System.out.println()?
			// Actually, both work
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			message.writeTo(System.out);

			out.flush();
			out.close();

			// Get the URL
			String url = prop.getProperty("mmsc-url");

			URL endpoint = new URL(url);

			response = con.call(message, endpoint);

			showResults(response);
			con.close();

			return null;
		} catch (Exception e) {
			e.printStackTrace();
			String fail = "Fail to send";
			return fail;
		}
	}

	public static void showResults(SOAPMessage reply) {
		SOAPPart sp = null;
		SOAPEnvelope envelope = null;
		try {
			sp = reply.getSOAPPart();
			envelope = sp.getEnvelope();
			SOAPBody body = envelope.getBody();
			body = envelope.getBody();

			System.out.println("IC SOAP Header<<");
			Iterator theMimeHdrIter = reply.getMimeHeaders().getAllHeaders();

			for (; theMimeHdrIter.hasNext();) {
				MimeHeader theHdr = (MimeHeader) theMimeHdrIter.next();

				System.out.println("Header=" + theHdr.getName() + ",Value=" + theHdr.getValue());
				if (theHdr.getName().matches("mmsc-code")) {
					if (theHdr.getValue().matches("1000")) {
						System.out.println("Send MMS OK,StatusCode=" + theHdr.getValue());
					} else {
						System.out.println("ERROR: MMS Failed, Statuscode=" + theHdr.getValue());
					}
				}
			}

			System.out.println("End IC SOAP Header<<");
			System.out.println("Actual Response from MM7 inteface:");

			// Create the message transformer
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();

			// Extract the content of the reply
			Source sourceContent = reply.getSOAPPart().getContent();

			// Set the output for the transformation
			StreamResult result = new StreamResult(System.out);
			transformer.transform(sourceContent, result);

			if (body.hasFault()) {
				SOAPFault newFault = body.getFault();
				Name code = newFault.getFaultCodeAsName();
				String string = newFault.getFaultString();
				String actor = newFault.getFaultActor();

				System.out.println("Send Error:SOAP fault contains: ");
				System.out.println("Fault code = " + code.getQualifiedName());
				System.out.println("Fault string = " + string);

				if (actor != null) {
					System.out.println("Fault actor = " + actor);
				}
			} else {
				System.out.println("SOAP Body OK");
			}

			System.out.println("Closing connection...");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void main(String... args) {
		prop = new Properties();
		try {
			prop.load(new FileInputStream("config.properties"));
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		sender();
	}

}
