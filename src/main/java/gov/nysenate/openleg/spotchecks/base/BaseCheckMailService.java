package gov.nysenate.openleg.spotchecks.base;

import gov.nysenate.openleg.config.Environment;
import gov.nysenate.openleg.common.util.FileIOUtils;
import gov.nysenate.openleg.common.util.MailUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.mail.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

public abstract class BaseCheckMailService {

    private static final Logger logger = LoggerFactory.getLogger(BaseCheckMailService.class);

    @Autowired
    protected Environment environment;

    @Autowired
    protected MailUtils mailUtils;

    protected void moveToArchive(Folder sourceFolder, Folder archive, Message... messages) throws MessagingException {
        sourceFolder.copyMessages(messages, archive);
        for (Message message : messages) {
            message.setFlag(Flags.Flag.DELETED, true);
        }
        sourceFolder.expunge();
    }

    protected void saveMessageBody(Message message, File file) throws MessagingException, IOException {
        if (message.isMimeType("text/*")) {
            FileIOUtils.write(file, (String) message.getContent());
        } else if (message.isMimeType("multipart/*")) {
            Multipart content = (Multipart) message.getContent();
            for (int i = 0; i < content.getCount(); i++) {
                Part part = content.getBodyPart(i);
                if (!Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
                    logger.info("\tsaving body of {} to {}", message.getSubject(), file.getAbsolutePath());
                    FileIOUtils.write(file, (String) part.getContent());
                }
            }
        }
    }

    protected void saveMessageAttachment(Message message, File file) throws MessagingException, IOException {
        if (message.isMimeType("multipart/*")) {
            Multipart content = (Multipart) message.getContent();
            for (int i = 0; i < content.getCount(); i++) {
                Part part = content.getBodyPart(i);
                if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
                    logger.info("\tSaving " + part.getFileName() + " to " + file.getAbsolutePath());
                    String attachment = IOUtils.toString(part.getInputStream(), Charset.defaultCharset());
                    FileIOUtils.write(file, attachment);
                }
            }
        }
    }

}
