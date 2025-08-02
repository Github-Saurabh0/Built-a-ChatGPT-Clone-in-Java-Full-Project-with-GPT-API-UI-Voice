package com.chatgpt.clone.ui;

import com.chatgpt.clone.util.Logger;

import javax.speech.AudioException;
import javax.speech.Central;
import javax.speech.EngineException;
import javax.speech.EngineStateError;
import javax.speech.synthesis.Synthesizer;
import javax.speech.synthesis.SynthesizerModeDesc;
import javax.speech.synthesis.Voice;
import java.beans.PropertyVetoException;
import java.util.Locale;

/**
 * Manages text-to-speech functionality using FreeTTS.
 */
public class VoiceManager {
    private static final Logger logger = new Logger(VoiceManager.class);
    
    private Synthesizer synthesizer;
    private boolean initialized = false;
    private boolean enabled = true;
    
    /**
     * Creates a new VoiceManager instance.
     */
    public VoiceManager() {
        try {
            initializeSynthesizer();
        } catch (Exception e) {
            logger.error("Failed to initialize speech synthesizer", e);
            // Set enabled to false if initialization fails
            enabled = false;
            initialized = false;
        }
    }
    
    /**
     * Initializes the speech synthesizer.
     */
    private void initializeSynthesizer() {
        try {
        // Set FreeTTS properties
        System.setProperty("freetts.voices", "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");
        
        // Create a synthesizer for English
        SynthesizerModeDesc desc = new SynthesizerModeDesc(
                null,          // engine name
                "general",     // mode name
                Locale.US,     // locale
                null,          // running
                null           // voice
        );
        
        // Create the synthesizer
        synthesizer = Central.createSynthesizer(desc);
        
        // Allocate the synthesizer
        synthesizer.allocate();
        
        // Select a voice
        Voice voice = new Voice(
                "kevin16",     // name
                Voice.GENDER_MALE,  // gender
                Voice.AGE_DONT_CARE,    // age
                null                // variant
        );
        synthesizer.getSynthesizerProperties().setVoice(voice);
        
        // Resume the synthesizer
        synthesizer.resume();
        
        initialized = true;
        logger.info("Speech synthesizer initialized");
        } catch (Exception e) {
            logger.error("Error initializing speech synthesizer", e);
            enabled = false;
            initialized = false;
        }
    }
    
    /**
     * Speaks the given text.
     * 
     * @param text The text to speak
     */
    public void speak(String text) {
        if (!enabled || !initialized) {
            logger.warning("Speech synthesis is not enabled or initialized");
            return;
        }
        
        try {
            // Speak the text
            synthesizer.speakPlainText(text, null);
            
            // Wait for the synthesizer to finish
            synthesizer.waitEngineState(Synthesizer.QUEUE_EMPTY);
        } catch (Exception e) {
            logger.error("Error speaking text", e);
        }
    }
    
    /**
     * Speaks the given text asynchronously.
     * 
     * @param text The text to speak
     */
    public void speakAsync(final String text) {
        if (!enabled || !initialized) {
            logger.warning("Speech synthesis is not enabled or initialized");
            return;
        }
        
        // Create a new thread to speak the text
        Thread speakThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Speak the text
                    synthesizer.speakPlainText(text, null);
                } catch (Exception e) {
                    logger.error("Error speaking text asynchronously", e);
                }
            }
        });
        
        speakThread.start();
    }
    
    /**
     * Checks if speech synthesis is enabled.
     * 
     * @return true if speech synthesis is enabled, false otherwise
     */
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * Sets whether speech synthesis is enabled.
     * 
     * @param enabled true to enable speech synthesis, false to disable it
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    /**
     * Cleans up resources used by the synthesizer.
     */
    public void cleanup() {
        if (initialized) {
            try {
                synthesizer.deallocate();
                logger.info("Speech synthesizer deallocated");
            } catch (Exception e) {
                logger.error("Error deallocating speech synthesizer", e);
            }
        }
    }
}