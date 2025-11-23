package com.beachbooking.service;

import com.beachbooking.model.entity.Prenotazione;
import com.beachbooking.model.entity.Tenant;
import com.beachbooking.model.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

/**
 * Service per l'invio di email.
 * Tutte le operazioni sono asincrone per non bloccare il thread principale.
 */
@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.base-url}")
    private String baseUrl;

    /**
     * Invia email di verifica dopo registrazione.
     */
    @Async
    public void sendVerificationEmail(User user, String token) {
        try {
            String subject = "Verifica il tuo account - Beach Booking";
            String verificationUrl = baseUrl + "/verify-email?token=" + token;

            String body = String.format("""
                Ciao %s,
                
                Benvenuto su Beach Booking!
                
                Per completare la registrazione, verifica il tuo indirizzo email cliccando sul link:
                %s
                
                Il link è valido per 24 ore.
                
                Se non hai richiesto questa registrazione, ignora questa email.
                
                Cordiali saluti,
                Il team di Beach Booking
                """, user.getNome(), verificationUrl);

            sendSimpleEmail(user.getEmail(), subject, body);

        } catch (Exception e) {
            System.err.println("Errore invio email verifica: " + e.getMessage());
        }
    }

    /**
     * Invia email di benvenuto dopo verifica.
     */
    @Async
    public void sendWelcomeEmail(User user, Tenant tenant) {
        try {
            String subject = "Benvenuto su Beach Booking!";

            String body = String.format("""
                Ciao %s,
                
                Il tuo account è stato verificato con successo!
                
                Ora puoi accedere al tuo stabilimento %s e iniziare a gestire le tue prenotazioni.
                
                Accedi qui: %s
                
                Buon lavoro!
                Il team di Beach Booking
                """, user.getNome(), tenant.getNomeStabilimento(), baseUrl);

            sendSimpleEmail(user.getEmail(), subject, body);

        } catch (Exception e) {
            System.err.println("Errore invio email benvenuto: " + e.getMessage());
        }
    }

    /**
     * Invia email di reset password.
     */
    @Async
    public void sendPasswordResetEmail(User user, String token) {
        try {
            String subject = "Reset Password - Beach Booking";
            String resetUrl = baseUrl + "/reset-password?token=" + token;

            String body = String.format("""
                Ciao %s,
                
                Hai richiesto il reset della password.
                
                Clicca sul link per reimpostare la password:
                %s
                
                Il link è valido per 24 ore.
                
                Se non hai richiesto il reset, ignora questa email.
                
                Cordiali saluti,
                Il team di Beach Booking
                """, user.getNome(), resetUrl);

            sendSimpleEmail(user.getEmail(), subject, body);

        } catch (Exception e) {
            System.err.println("Errore invio email reset: " + e.getMessage());
        }
    }

    /**
     * Invia conferma prenotazione al cliente.
     */
    @Async
    public void sendBookingConfirmation(Prenotazione prenotazione, User user, Tenant tenant) {
        try {
            String subject = "Conferma Prenotazione - " + tenant.getNomeStabilimento();

            String body = String.format("""
                Ciao %s,
                
                La tua prenotazione è stata confermata!
                
                Dettagli:
                - Codice: %s
                - Stabilimento: %s
                - Dal: %s
                - Al: %s
                - Importo: €%.2f
                
                Ti aspettiamo!
                
                Per modifiche o cancellazioni, contattaci:
                %s - %s
                
                Cordiali saluti,
                %s
                """,
                    user.getNome(),
                    prenotazione.getCodicePrenotazione(),
                    tenant.getNomeStabilimento(),
                    prenotazione.getDataInizio(),
                    prenotazione.getDataFine(),
                    prenotazione.getPrezzoTotale(),
                    tenant.getTelefono(),
                    tenant.getEmail(),
                    tenant.getNomeStabilimento()
            );

            sendSimpleEmail(user.getEmail(), subject, body);

        } catch (Exception e) {
            System.err.println("Errore invio email conferma prenotazione: " + e.getMessage());
        }
    }

    /**
     * Invia notifica cancellazione prenotazione.
     */
    @Async
    public void sendBookingCancellation(Prenotazione prenotazione, User user, Tenant tenant, String motivo) {
        try {
            String subject = "Prenotazione Cancellata - " + tenant.getNomeStabilimento();

            String body = String.format("""
                Ciao %s,
                
                La tua prenotazione è stata cancellata.
                
                Dettagli:
                - Codice: %s
                - Stabilimento: %s
                - Motivo: %s
                
                Per informazioni contattaci:
                %s - %s
                
                Cordiali saluti,
                %s
                """,
                    user.getNome(),
                    prenotazione.getCodicePrenotazione(),
                    tenant.getNomeStabilimento(),
                    motivo,
                    tenant.getTelefono(),
                    tenant.getEmail(),
                    tenant.getNomeStabilimento()
            );

            sendSimpleEmail(user.getEmail(), subject, body);

        } catch (Exception e) {
            System.err.println("Errore invio email cancellazione: " + e.getMessage());
        }
    }

    /**
     * Invia promemoria prenotazione (1 giorno prima).
     */
    @Async
    public void sendBookingReminder(Prenotazione prenotazione, User user, Tenant tenant) {
        try {
            String subject = "Promemoria Prenotazione - " + tenant.getNomeStabilimento();

            String body = String.format("""
                Ciao %s,
                
                Ti ricordiamo che domani inizia la tua prenotazione!
                
                Codice: %s
                Data: %s
                
                Ti aspettiamo presso:
                %s
                %s
                
                Per informazioni: %s
                
                A presto!
                %s
                """,
                    user.getNome(),
                    prenotazione.getCodicePrenotazione(),
                    prenotazione.getDataInizio(),
                    tenant.getNomeStabilimento(),
                    tenant.getIndirizzo(),
                    tenant.getTelefono(),
                    tenant.getNomeStabilimento()
            );

            sendSimpleEmail(user.getEmail(), subject, body);

        } catch (Exception e) {
            System.err.println("Errore invio email promemoria: " + e.getMessage());
        }
    }

    /**
     * Invia conferma pagamento.
     */
    @Async
    public void sendPaymentConfirmation(Prenotazione prenotazione, User user, Tenant tenant, String metodoPagamento) {
        try {
            String subject = "Pagamento Confermato - " + tenant.getNomeStabilimento();

            String body = String.format("""
                Ciao %s,
                
                Il tuo pagamento è stato confermato!
                
                Dettagli:
                - Codice Prenotazione: %s
                - Importo: €%.2f
                - Metodo: %s
                
                Grazie per aver scelto %s!
                
                Cordiali saluti,
                %s
                """,
                    user.getNome(),
                    prenotazione.getCodicePrenotazione(),
                    prenotazione.getPrezzoTotale(),
                    metodoPagamento,
                    tenant.getNomeStabilimento(),
                    tenant.getNomeStabilimento()
            );

            sendSimpleEmail(user.getEmail(), subject, body);

        } catch (Exception e) {
            System.err.println("Errore invio email conferma pagamento: " + e.getMessage());
        }
    }

    /**
     * Metodo base per invio email semplice.
     */
    private void sendSimpleEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);

            mailSender.send(message);

        } catch (Exception e) {
            System.err.println("Errore invio email: " + e.getMessage());
            throw new RuntimeException("Impossibile inviare email", e);
        }
    }

    /**
     * Metodo per invio email HTML (opzionale, per future implementazioni).
     */
    @Async
    public void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }
}