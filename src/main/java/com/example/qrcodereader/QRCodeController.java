package com.example.qrcodereader;

import com.example.qrcodereader.models.ProblemResponse;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

@RestController
@RequestMapping("/challenges/reading_qr")
public class QRCodeController {
    @GetMapping("/problem")
    public ResponseEntity<String> getProblem() {
        String problemUrl = "https://hackattic.com/challenges/reading_qr/problem?access_token=92c85f64a327e7df";

        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<ProblemResponse> problemResponse = restTemplate.getForEntity(problemUrl, ProblemResponse.class);
        String imageUrl = problemResponse.getBody().getImageUrl();
        // Download the image and decode the QR code
        String numericCode = decodeQRCode(imageUrl);
        submitAnswer(numericCode);
        String jsonResponse = "{\"code\":\"" + numericCode + "\"}";

        return ResponseEntity.ok(jsonResponse);
    }

    @PostMapping("/challenges/reading_qr/solve")
    public ResponseEntity<String> solveProblem(@RequestBody String code) {
        return ResponseEntity.ok("Solution submitted: " + code);
    }

    private String decodeQRCode(String imageUrl) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            byte[] imageBytes = restTemplate.getForObject(imageUrl, byte[].class);

            ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
            BufferedImage bufferedImage = ImageIO.read(bis);

            MultiFormatReader reader = new MultiFormatReader();
            BinaryBitmap binaryBitmap = new BinaryBitmap(
                    new HybridBinarizer(new BufferedImageLuminanceSource(bufferedImage))
            );

            Result result = reader.decode(binaryBitmap);

            String qrCodeData = result.getText();
            String numericCode = qrCodeData.replaceAll("[^0-9-]", "");

            return numericCode;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (NotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void submitAnswer(String code) {
        try {
            String solveEndpoint = "https://hackattic.com/challenges/reading_qr/solve?access_token=92c85f64a327e7df";

            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String requestBody = "{\"code\":\"" + code + "\"}";
            HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(solveEndpoint, requestEntity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("Answer submitted successfully: " + code);
                System.out.println("Server response: " + response.getBody());
            } else {
                System.err.println("Failed to submit answer: " + response.getStatusCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

