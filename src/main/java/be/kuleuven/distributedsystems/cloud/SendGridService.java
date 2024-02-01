package be.kuleuven.distributedsystems.cloud;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;

import java.io.IOException;

public class SendGridService {
    private static final String API_KEY = "fake-api-key";

    /**
     * Email the customer once their booking has finished processing to let them know whether it
     * succeeded or not (feedback channel).
     *
     * @param customer The customer's email
     * @param success A boolean indicating whether the booking succeeded or not
     * @param booking_reference The booking reference, in the case that the booking succeeded
     */
    public static void SendEmail(String customer, Boolean success, String booking_reference) throws IOException {
        Email from = new Email("conorpeter.finlay@student.kuleuven.be");
        Email to = new Email(customer);
        String subject;
        Content content;
        if(success){
            subject = "Booking success";
            content = new Content("text/html",
                    "Dear customer,<br><br>" +
                            "We are pleased to inform you that your booking with reference " + booking_reference + " has been confirmed.<br>" +
                            "Please sign in on <a href='https://ds-part-2.ew.r.appspot.com/'>https://ds-part-2.ew.r.appspot.com/</a> to view your booking.<br><br>" +
                            "Getting you from A to B,<br>" +
                            "DnetTickets."
            );
        } else {
            subject = "Booking failure";
            content = new Content("text/html",
                    "Dear customer,<br><br>" +
                            "We regret to inform you that we were unable to confirm your booking with reference " + booking_reference + ".<br>" +
                            "Some tickets in this booking may no longer be available, so we recommend signing into your account at " +
                            "<a href='https://ds-part-2.ew.r.appspot.com/'>https://ds-part-2.ew.r.appspot.com/</a> and attempting to select your desired seats again.<br>" +
                            "If the problem persists, please reach out to our dedicated support.<br><br>" +
                            "Getting you from A to B,<br>" +
                            "DnetTickets.");
        }
        Mail mail = new Mail(from, subject, to, content);

        SendGrid sg = new SendGrid(API_KEY);
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);
            System.out.println(response.getStatusCode());
            System.out.println(response.getBody());
            System.out.println(response.getHeaders());
        } catch (IOException ex) {
            throw ex;
        }
    }
}
