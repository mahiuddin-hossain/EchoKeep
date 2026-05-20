package com.suitexen.ecokeep.services;

import android.content.Context;
import android.net.Uri;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.IOException;

public class ReceiptScanner {

    public interface ScanCallback {
        void onSuccess(String extractedText);
        void onFailure(Exception e);
    }

    public static void scanReceipt(Context context, Uri imageUri, ScanCallback callback) {
        try {
            InputImage image = InputImage.fromFilePath(context, imageUri);
            TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

            recognizer.process(image)
                    .addOnSuccessListener(visionText -> {
                        String resultText = visionText.getText();
                        if (!resultText.isEmpty()) {
                            callback.onSuccess(resultText);
                        } else {
                            callback.onFailure(new Exception("No text found in receipt"));
                        }
                    })
                    .addOnFailureListener(callback::onFailure);

        } catch (IOException e) {
            callback.onFailure(e);
        }
    }
}