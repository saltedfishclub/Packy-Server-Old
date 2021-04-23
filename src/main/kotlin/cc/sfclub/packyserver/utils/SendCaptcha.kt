package cc.sfclub.packyserver.utils

import sun.nio.cs.UTF_8
import java.util.Properties
import javax.mail.Message
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.MimeMessage
import javax.mail.internet.InternetAddress

object SendCaptcha {
    fun send(to: String, sender: String, pass: String, host: String, captcha: String, name: String) {
        val props = Properties()
        props.setProperty("mail.smtp.auth", "true")
        props.setProperty("mail.transport.protocol", "smtp")
        props.setProperty("mail.smtp.host", host)
        val session: Session = Session.getInstance(props)
        session.debug = true
        var msg: Message = MimeMessage(session)
        val transport: Transport = session.transport

        msg.setFrom(InternetAddress(sender, "Packy Group", "UTF-8"))
        msg.setRecipient(MimeMessage.RecipientType.TO, InternetAddress(to, name, "UTF-8"))
        msg.subject = "Packy邮箱验证"
        msg.setContent("验证码: $captcha", "text/html;charset=UTF-8")
        msg.saveChanges()

        transport.connect(sender, pass)
        transport.sendMessage(msg, msg.allRecipients)
        transport.close()
    }
}