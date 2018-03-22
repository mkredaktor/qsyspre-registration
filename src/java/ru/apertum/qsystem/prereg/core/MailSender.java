/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.apertum.qsystem.prereg.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Scanner;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggerFactory;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Sessions;
import ru.apertum.qsystem.common.Uses;
import ru.apertum.qsystem.prereg.Client;
import ru.apertum.qsystem.prereg.SiteProperty;

/**
 *
 * @author Evgeniy Egorov
 */
public class MailSender {
    private Logger logger = Logger.getLogger(MailSender.class);
    private static final String FROM_KEY = "mail.smtp.from";
    private static final String DEFAULT_FROM_ADDRESS = "test@touch.ua";

    public String l(String resName) {
        return Labels.getLabel(resName);
    }

    private MailSender() {
        try {
            ctx = new InitialContext();
        } catch (NamingException ex) {
            throw new RuntimeException(ex);
        }

        String fl = ((SiteProperty) Sessions.getCurrent().getAttribute("PROPS")).getMailContentFile();
        if (fl != null && !fl.isEmpty() && new File(fl).exists()) {
            File f = new File(fl);
            subject = f.getName().replaceFirst("\\..*", "");
            try {
                content = new String(Files.readAllBytes(f.toPath()), "utf-8");
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    private String subject = null;
    private String content = null;

    public static MailSender getInstance() {
        return MailSenderHolder.INSTANCE;
    }

    private static class MailSenderHolder {

        private static final MailSender INSTANCE = new MailSender();
    }
    final private InitialContext ctx;

    public synchronized void sendMessage(Client client) throws UnsupportedEncodingException, MessagingException, NamingException {

        final Session mailSession = (Session) ctx.lookup("QSYSPREREG-MAIL");
        mailSession.setDebug(true);
        final Message msg = new MimeMessage(mailSession);
        msg.setSubject("[" + ((SiteProperty) Sessions.getCurrent().getAttribute("PROPS")).getTitle() + "] " + (subject == null ? l("pre_reg") : subject) + " " + client.getAdvClient().getId());

        String propFrom = mailSession.getProperty(FROM_KEY);
        if(propFrom == null) {
            propFrom = mailSession.getProperty("mail-smtp-from");
        }
        if(propFrom == null) {
            propFrom = mailSession.getProperty("mail-from");
        }
        String from = propFrom != null ? propFrom : DEFAULT_FROM_ADDRESS;
        msg.setFrom(new InternetAddress(from));

        System.out.println(mailSession.getProperties().toString());
        logger.info(mailSession.getProperties().toString());

        msg.setRecipient(RecipientType.TO, new InternetAddress(client.getEmail(), client.toString()));
        final String mess;

        if (content == null) {
            mess = ((SiteProperty) Sessions.getCurrent().getAttribute("PROPS")).getCaption() + "\n\n\n"
                    + "   Здравствуйте"
                    + (client.getSourname() != null && client.getName() != null && client.getMiddlename() != null
                            ? " " + client.getSourname() + " " + client.getName() + " " + client.getMiddlename() : "")
                    + ".\n\n"
                    + "Вы зарегистрированы предварительно для получения услуги \"" + client.getService().getName() + "\".\n"
                    + "Номер регистрации " + client.getAdvClient().getId() + ".\n"
                    + "Этот номер необходимо ввести при получении талона на киоске регистрации, не потеряйте его.\n"
                    + "Вам необходимо прийти " + Uses.format_dd_MM_yyyy.format(client.getDate()) + /*" c " + client.getStartT() + " до "*/ " к " + client.getFinishT() + ".\n\n\n"
                    + client.getService().getInput_caption() + "  " + (client.getInputData() != null ? client.getInputData() : "") + ".\n\n"
                    + client.getService().getPreInfoPrintText() + "\n\n\n"
                    + "Это письмо выслано автоматически. Не отвечайте на него.\n"
                    + "QSystem - Copyright 2016 Apertum Projects";
        } else {
            mess = content
                    .replace("${name}", client.getName() == null ? "" : client.getName())
                    .replace("${sourname}", client.getSourname() == null ? "" : client.getSourname())
                    .replace("${meddlename}", client.getMiddlename() == null ? "" : client.getMiddlename())
                    .replace("${service}", client.getService().getName())
                    .replace("${number}", client.getAdvClient().getId().toString())
                    .replace("${date}", Uses.format_dd_MM_yyyy.format(client.getDate()))
                    .replace("${time}", client.getFinishT())
                    .replace("${input_caption}", client.getService().getInput_caption() == null ? "" : client.getService().getInput_caption())
                    .replace("${input_data}", (client.getInputData() != null ? client.getInputData() : ""))
                    .replace("${info_text}", client.getService().getPreInfoPrintText());
        }
        msg.setText(mess);
        Transport.send(msg);

    }

    static final String ENCODING = "UTF-8";

    public static void sendReporterMailAtFon(String subject, String content, String addrs_to, final String attachment) {
        final Thread t = new Thread(() -> {
            final File attach = new File(attachment);
            try {
                sendReporterMail(subject, content, addrs_to, attach.exists() ? attach : null);
            } catch (MessagingException | UnsupportedEncodingException ex) {
                throw new RuntimeException("Send mail was failure.", ex);
            } catch (IOException ex) {
                throw new RuntimeException("2. Send mail was failure.", ex);
            }
        });
        t.start();
    }

    public static void sendReporterMail(String subject, String content, String addrs_to, File attachment) throws MessagingException, UnsupportedEncodingException, IOException {
        Properties props = fetchConfig();

        final Authenticator auth = new MyAuthenticator(props.getProperty("mail.smtp.user"), props.getProperty("mail.password"));
        final Session session = Session.getDefaultInstance(props, auth);
        session.setDebug(true);

        final MimeMessage msg = new MimeMessage(session);

        String to = addrs_to == null ? props.getProperty("mail.smtp.to") : addrs_to;

        String propFrom = props.getProperty(FROM_KEY);
        String from = propFrom != null ? propFrom : DEFAULT_FROM_ADDRESS;
        msg.setFrom(new InternetAddress(from));

        to = to.replaceAll("  ", " ").replaceAll(" ;", ";").replaceAll(" ,", ",").replaceAll(", ", ",").replaceAll("; ", ",").replaceAll(";", ",").replaceAll(" ", ",").replaceAll(",,", ",");
        final String[] ss = to.split(",");
        final ArrayList<InternetAddress> adresses = new ArrayList<>();
        for (String str : ss) {
            if (!"".equals(str.trim())) {
                adresses.add(new InternetAddress(str.trim()));
            }
        }

        msg.setRecipients(Message.RecipientType.TO, adresses.toArray(new InternetAddress[0]));
        msg.setHeader("Content-Type", "text/html;charset=\"UTF-8\"");
        msg.setSubject(subject == null ? props.getProperty("mail.subject") : subject, "UTF-8");

        final BodyPart messageBodyPart = new MimeBodyPart();

        File f = new File(props.getProperty("mail.content"));
        if (f.exists()) {
            final Scanner s;
            try {
                s = new Scanner(f);
            } catch (FileNotFoundException ex) {
                throw new RuntimeException(ex);
            }
            final StringBuilder sb = new StringBuilder();
            while (s.hasNext()) {
                sb.append(s.next());
            }
            messageBodyPart.setContent(content == null ? sb.toString() : content, "text/html; charset=\"UTF-8\"");
            sb.setLength(0);
        } else {
            messageBodyPart.setContent(content == null ? props.getProperty("mail.content") : content, "text/plain; charset=\"UTF-8\"");
        }

        final Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(messageBodyPart);

        if (attachment != null) {
            final MimeBodyPart attachmentBodyPart = new MimeBodyPart();
            final DataSource source = new FileDataSource(attachment);
            attachmentBodyPart.setDataHandler(new DataHandler(source));
            attachmentBodyPart.setFileName(MimeUtility.encodeText(source.getName()));
            multipart.addBodyPart(attachmentBodyPart);
        }

        msg.setContent(multipart);

        Transport.send(msg);
    }

    /**
     * Open a specific text file containing mail server parameters, and populate a corresponding Properties object.
     *
     * @return props
     */
    public static Properties fetchConfig() throws IOException {
        if (fMailServerConfig != null) {
            return fMailServerConfig;
        }
        fMailServerConfig = new Properties();
        InputStream input = null;
        try {
            //If possible, one should try to avoid hard-coding a path in this
            //manner; in a web application, one should place such a file in
            //WEB-INF, and access it using ServletContext.getResourceAsStream.
            //Another alternative is Class.getResourceAsStream.
            //This file contains the javax.mail config properties mentioned above.
            input = new FileInputStream("config/reporter.properties");
            final InputStreamReader inR = new InputStreamReader(input, "UTF-8");
            fMailServerConfig.load(inR);
        } finally {
            if (input != null) {
                input.close();
            }
        }
        return fMailServerConfig;
    }
    private static Properties fMailServerConfig;
}

class MyAuthenticator extends Authenticator {

    private final String user_;
    private final String password_;

    MyAuthenticator(String user, String password) {
        this.user_ = user;
        this.password_ = password;
    }

    @Override
    public PasswordAuthentication getPasswordAuthentication() {
        String user = user_;
        String password = password_;
        return new PasswordAuthentication(user, password);
    }
}
