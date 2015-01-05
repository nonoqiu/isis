package org.apache.isis.core.runtime.services.email;

import java.util.List;
import java.util.Properties;
import javax.annotation.PostConstruct;
import com.google.common.base.Strings;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.email.EmailService;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.runtime.system.context.IsisContext;

/**
 * A service that sends email notifications when specific events occur
 */
@com.google.inject.Singleton // necessary because is registered in and injected by google guice
@DomainService
public class EmailServiceDefault implements EmailService {

    private static final Logger LOG = LoggerFactory.getLogger(EmailServiceDefault.class);

    //region > constants

    private static final String ISIS_SERVICE_EMAIL_SENDER_ADDRESS = "isis.service.email.sender.address";
    private static final String ISIS_SERVICE_EMAIL_SENDER_PASSWORD = "isis.service.email.sender.password";

    private static final String ISIS_SERVICE_EMAIL_SENDER_HOSTNAME = "isis.service.email.sender.hostname";
    private static final String ISIS_SERVICE_EMAIL_SENDER_HOSTNAME_DEFAULT = "smtp.gmail.com";

    private static final String ISIS_SERVICE_EMAIL_PORT = "isis.service.email.port";
    private static final int ISIS_SERVICE_EMAIL_PORT_DEFAULT = 587;

    private static final String ISIS_SERVICE_EMAIL_TLS_ENABLED = "isis.service.email.tls.enabled";
    private static final boolean ISIS_SERVICE_EMAIL_TLS_ENABLED_DEFAULT = true;

    private String senderEmailAddress;
    private String senderEmailPassword;
    private Integer senderEmailPort;
    //endregion

    //region > init
    private boolean initialized;

    /**
     * Loads responsive email templates borrowed from http://zurb.com/ink/templates.php (Basic)
     */
    @PostConstruct
    @Programmatic
    public void init() {

        if(initialized) {
            return;
        }

        senderEmailAddress = getSenderEmailAddress();
        senderEmailPassword = getSenderEmailPassword();
        senderEmailPort = getSenderEmailPort();

        initialized = true;

        if(!isConfigured()) {
            LOG.warn("NOT configured");
        } else {
            LOG.debug("configured");
        }
    }

    protected String getSenderEmailAddress() {
        return getConfiguration().getString(ISIS_SERVICE_EMAIL_SENDER_ADDRESS);
    }

    protected String getSenderEmailPassword() {
        return getConfiguration().getString(ISIS_SERVICE_EMAIL_SENDER_PASSWORD);
    }

    protected String getSenderEmailHostName() {
        return getConfiguration().getString(ISIS_SERVICE_EMAIL_SENDER_HOSTNAME, ISIS_SERVICE_EMAIL_SENDER_HOSTNAME_DEFAULT);
    }

    protected Integer getSenderEmailPort() {
        return getConfiguration().getInteger(ISIS_SERVICE_EMAIL_PORT, ISIS_SERVICE_EMAIL_PORT_DEFAULT);
    }

    protected Boolean getSenderEmailTlsEnabled() {
        return getConfiguration().getBoolean(ISIS_SERVICE_EMAIL_TLS_ENABLED, ISIS_SERVICE_EMAIL_TLS_ENABLED_DEFAULT);
    }
    //endregion

    //region > isConfigured

    @Override
    public boolean isConfigured() {
        return !Strings.isNullOrEmpty(senderEmailAddress) && !Strings.isNullOrEmpty(senderEmailPassword);
    }
    //endregion

    //region > send

    @Override
    public boolean send(final List<String> toList, final List<String> ccList, final List<String> bccList, final String subject, final String body) {

        try {

            final Email email = new HtmlEmail();
            email.setAuthenticator(new DefaultAuthenticator(senderEmailAddress, senderEmailPassword));
            email.setHostName(getSenderEmailHostName());
            email.setSmtpPort(senderEmailPort);
            email.setStartTLSEnabled(getSenderEmailTlsEnabled());

            final Properties properties = email.getMailSession().getProperties();

            // TODO ISIS-987: check whether all these are required and extract as configuration settings
            properties.put("mail.smtps.auth", "true");
            properties.put("mail.debug", "true");
            properties.put("mail.smtps.port", "" + senderEmailPort);
            properties.put("mail.smtps.socketFactory.port", "" + senderEmailPort);
            properties.put("mail.smtps.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            properties.put("mail.smtps.socketFactory.fallback", "false");
            properties.put("mail.smtp.starttls.enable", "" + getSenderEmailTlsEnabled());

            email.setFrom(senderEmailAddress);

            email.setSubject(subject);
            email.setMsg(body);

            if(notEmpty(toList)) {
                email.addTo(toList.toArray(new String[toList.size()]));
            }
            if(notEmpty(ccList)) {
                email.addCc(ccList.toArray(new String[ccList.size()]));
            }
            if(notEmpty(bccList)) {
                email.addBcc(bccList.toArray(new String[bccList.size()]));
            }

            email.send();

        } catch (EmailException ex) {
            LOG.error("An error occurred while trying to send an email about user email verification", ex);
            return false;
        }

        return true;
    }
    //endregion

    //region > helper methods
    private boolean notEmpty(final List<String> toList) {
        return toList != null && !toList.isEmpty();
    }
    //endregion

    //region > dependencies
    protected IsisConfiguration getConfiguration() {
        return IsisContext.getConfiguration();
    }
    //endregion



}
