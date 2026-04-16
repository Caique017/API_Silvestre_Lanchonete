package com.silvestre_lanchonete.auth_service.service;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;

@Service
public class EmailService {

    @Value("${aws.ses.sender-email}")
    private String senderEmail;

    public void sendEmail(String to, String subject, String htmlContent) {

        try (SesClient client = SesClient.builder().region(Region.US_EAST_1).build()) {

            SendEmailRequest request = SendEmailRequest.builder()
                    .destination(Destination.builder().toAddresses(to).build())
                    .message(Message.builder()
                            .subject(Content.builder().data(subject).charset("UTF-8").build())
                            .body(Body.builder()
                                    .html(Content.builder().data(htmlContent).charset("UTF-8").build())
                                    .build())
                            .build())
                    .source(senderEmail)
                    .build();

            client.sendEmail(request);
            System.out.println("✅ E-mail enviado com sucesso via AWS SES para: " + to);

        } catch (SesException e) {
            System.err.println("❌ Erro da AWS ao enviar e-mail: " + e.awsErrorDetails().errorMessage());
        } catch (Exception e) {
            System.err.println("❌ Erro inesperado no envio de e-mail: " + e.getMessage());
        }
    }
}
