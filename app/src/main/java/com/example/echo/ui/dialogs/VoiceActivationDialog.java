package com.example.echo.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.example.echo.R;
import com.example.echo.services.assistant.EchoAssistantService;
import com.example.echo.services.assistant.SpeechRecognitionService;
import com.example.echo.services.assistant.TextToSpeechService;

public class VoiceActivationDialog extends DialogFragment {
    private EchoAssistantService assistantService;
    private TextToSpeechService ttsService;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_voice_activation, null);

        assistantService = new EchoAssistantService(getContext());
        ttsService = new TextToSpeechService(getContext(), status -> {});

        // Start speech recognition
        SpeechRecognitionService speechService = new SpeechRecognitionService(getContext(), new SpeechRecognitionService.SpeechRecognitionCallback() {
            @Override
            public void onResult(String result) {
                assistantService.processQuery(result, new EchoAssistantService.AssistantCallback() {
                    @Override
                    public void onResponse(String response) {
                        ttsService.speak(response);
                        // Save to conversation
                    }

                    @Override
                    public void onError(String errorMessage) {
                        ttsService.speak("Sorry, an error occurred.");
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                ttsService.speak("Please try again.");
            }
        });
        speechService.startListening();

        builder.setView(view)
                .setNegativeButton("Cancel", (dialog, id) -> {
                    speechService.stopListening();
                    ttsService.shutdown();
                    dialog.dismiss();
                });

        return builder.create();
    }
}