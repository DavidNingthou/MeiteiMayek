package net.devloper.mm;

import android.app.Activity;
import android.content.Context;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.runtime.AndroidNonvisibleComponent;
import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.appinventor.components.runtime.EventDispatcher;
import java.util.*;

@DesignerComponent(version = 1,
    description = "Meitei Mayek Transliterator Extension",
    category = ComponentCategory.EXTENSION,
    nonVisible = true,
    iconName = "images/extension.png")
@SimpleObject(external = true)
public class MM extends AndroidNonvisibleComponent {

    public MM(ComponentContainer container) {
        super(container.$form());
    }

    private static final String[][] MEITEI_MAYEK_PHONEMES = {
        {"a", "true", "\uABE5", "ꯑ", "false", ""},
        {"aa", "true", "\uABE5", "ꯑ", "false", ""},
        {"b", "false", "", "ꯕ", "false", ""},
        {"bh", "false", "", "ꯚ", "false", ""},
        {"c", "false", "", "ꯆ", "false", ""},
        {"ch", "false", "", "ꯆ", "false", ""},
        {"d", "false", "", "ꯗ", "false", ""},
        {"dh", "false", "", "ꯙ", "false", ""},
        {"e", "true", "\uABE6", "ꯑ\uABE6", "false", ""},
        {"ee", "true", "\uABE4", "ꯏ", "false", ""},
        {"ei", "true", "\uABE9", "ꯑ\uABE9", "false", ""},
        {"f", "false", "", "ꯐ", "false", ""},
        {"g", "false", "", "ꯒ", "false", ""},
        {"gh", "false", "", "ꯘ", "false", ""},
        {"h", "false", "", "ꯍ", "false", ""},
        {"i", "true", "\uABE4", "ꯏ", "true", "ꯢ"},
        {"j", "false", "", "ꯖ", "false", ""},
        {"jh", "false", "", "ꯓ", "false", ""},
        {"k", "false", "", "ꯀ", "true", "ꯛ"},
        {"kh", "false", "", "ꯈ", "false", ""},
        {"l", "false", "", "ꯂ", "true", "ꯜ"},
        {"m", "false", "", "ꯃ", "true", "ꯝ"},
        {"n", "false", "", "ꯅ", "true", "ꯟ"},
        {"ng", "false", "\uABEA", "ꯉ", "true", "ꯡ"},
        {"o", "true", "\uABE3", "ꯑ\uABE3", "false", ""},
        {"oo", "true", "\uABE8", "ꯎ", "false", ""},
        {"ou", "true", "\uABE7", "ꯑ\uABE7", "false", ""},
        {"p", "false", "", "ꯄ", "true", "ꯞ"},
        {"ph", "false", "", "ꯐ", "false", ""},
        {"r", "false", "", "ꯔ", "false", ""},
        {"s", "false", "", "ꯁ", "false", ""},
        {"t", "false", "", "ꯇ", "true", "ꯠ"},
        {"th", "false", "", "ꯊ", "false", ""},
        {"u", "true", "\uABE8", "ꯎ", "false", ""},
        {"v", "false", "", "ꯚ", "false", ""},
        {"w", "false", "", "ꯋ", "false", ""},
        {"y", "false", "", "ꯌ", "false", ""},
        {"z", "false", "", "ꯖ", "false", ""},
        {".", "false", "", "\uABEB", "false", ""},
        {"q", "false", "", "ꯀ\uABEDꯋ", "false", ""},
        {"x", "false", "", "ꯀ\uABEDꯁ", "false", ""}
    };

    private static final Map<String, String> MEITEI_MAYEK_NUMBERS = new HashMap<>();
    static {
        MEITEI_MAYEK_NUMBERS.put("꯰", "0");
        MEITEI_MAYEK_NUMBERS.put("꯱", "1");
        MEITEI_MAYEK_NUMBERS.put("꯲", "2");
        MEITEI_MAYEK_NUMBERS.put("꯳", "3");
        MEITEI_MAYEK_NUMBERS.put("꯴", "4");
        MEITEI_MAYEK_NUMBERS.put("꯵", "5");
        MEITEI_MAYEK_NUMBERS.put("꯶", "6");
        MEITEI_MAYEK_NUMBERS.put("꯷", "7");
        MEITEI_MAYEK_NUMBERS.put("꯸", "8");
        MEITEI_MAYEK_NUMBERS.put("꯹", "9");
    }

    private static final String[][] MEITEI_MAYEK_APUN_MAYEK_RULES = {
        {"b", "r"},
        {"dh", "r"},
        {"dh", "y"},
        {"f", "r"},
        {"g", "r"},
        {"g", "y"},
        {"j", "r"},
        {"j", "y"},
        {"k", "w"},
        {"k", "y"},
        {"kh", "r"},
        {"kh", "w"},
        {"n", "y"},
        {"p", "r"},
        {"p", "y"},
        {"ph", "r"},
        {"s", "w"},
        {"s", "y"},
        {"sh", "w"},
        {"sh", "y"},
        {"t", "r"},
        {"th", "r"},
        {"v", "y"}
    };

    private static class Phoneme {
        String phoneme;
        boolean isVowel;
        String asVowel;
        String asConsonant;
        boolean canBeLonsum;
        String asLonsum;
        boolean isNumeric;
        boolean isUnknown;

        Phoneme(String phoneme, boolean isVowel, String asVowel, String asConsonant, boolean canBeLonsum, String asLonsum, boolean isNumeric) {
            this.phoneme = phoneme;
            this.isVowel = isVowel;
            this.asVowel = asVowel;
            this.asConsonant = asConsonant;
            this.canBeLonsum = canBeLonsum;
            this.asLonsum = asLonsum;
            this.isNumeric = isNumeric;
            this.isUnknown = phoneme.isEmpty() || !(Character.isLetterOrDigit(phoneme.charAt(0)) || Character.isDigit(phoneme.charAt(0)));
        }
    }

    private static class Mapper {
        Map<String, Phoneme> phonemes = new HashMap<>();
        Set<String> apunMayekPhonemes = new HashSet<>();

        Mapper() {
            for (String[] phonemeData : MEITEI_MAYEK_PHONEMES) {
                phonemes.put(phonemeData[0], new Phoneme(
                    phonemeData[0],
                    Boolean.parseBoolean(phonemeData[1]),
                    phonemeData[2],
                    phonemeData[3],
                    Boolean.parseBoolean(phonemeData[4]),
                    phonemeData[5],
                    false
                ));
            }
            for (Map.Entry<String, String> entry : MEITEI_MAYEK_NUMBERS.entrySet()) {
                phonemes.put(entry.getValue(), new Phoneme(entry.getValue(), false, "", entry.getKey(), false, "", true));
            }
            for (String[] rule : MEITEI_MAYEK_APUN_MAYEK_RULES) {
                apunMayekPhonemes.add(rule[0] + "-" + rule[1]);
            }
        }

        Phoneme mapToPhonemeOrNull(String curr, String next) {
            return phonemes.get(curr + next);
        }

        Phoneme mapToPhoneme(String curr) {
            return phonemes.getOrDefault(curr, new Phoneme(curr, false, "", curr, false, "", false));
        }

        boolean isApunMayekPhonemesCombo(Phoneme one, Phoneme two) {
            return apunMayekPhonemes.contains(one.phoneme + "-" + two.phoneme);
        }
    }

    private static final Phoneme PHI = new Phoneme("", false, "", "", false, "", false);
    private static final Phoneme APUN_MAYEK_AS_PHONEME = new Phoneme("\uABED", false, "", "\uABED", false, "", false);

    private enum CVCState {
        NONE, CONSONANT, VOWEL
    }

    private enum OutputMode {
        VOWEL, CONSONANT, LONSUM
    }

    private static class PhonemeOutput {
        Phoneme phoneme;
        OutputMode outputMode;

        PhonemeOutput(Phoneme phoneme, OutputMode outputMode) {
            this.phoneme = phoneme;
            this.outputMode = outputMode;
        }

        String getOutput() {
            switch (outputMode) {
                case VOWEL: return phoneme.asVowel;
                case CONSONANT: return phoneme.asConsonant;
                case LONSUM: return phoneme.asLonsum;
                default: return phoneme.asConsonant;
            }
        }
    }

    private static class MeiteiMayekTransliterator {
        private final Mapper MAPPER = new Mapper();

        String transliterate(String text) {
            List<Phoneme> phonemes = new ArrayList<>();
            text = text != null ? text.toLowerCase() : "";
            for (int i = 0; i < text.length(); i++) {
                String curr = String.valueOf(text.charAt(i));
                String next = i + 1 < text.length() ? String.valueOf(text.charAt(i + 1)) : "";
                
                Phoneme digraphPhoneme = MAPPER.mapToPhonemeOrNull(curr + next, "");
                if (digraphPhoneme != null) {
                    phonemes.add(digraphPhoneme);
                    i++; // Skip next character as it's part of the digraph
                } else {
                    phonemes.add(MAPPER.mapToPhoneme(curr));
                }
            }
            return convertToMMCVC(phonemes);
        }

        private String convertToMMCVC(List<Phoneme> phonemes) {
            List<PhonemeOutput> output = new ArrayList<>();
            CVCState state = CVCState.NONE;
            PhonemeOutput prev = new PhonemeOutput(PHI, OutputMode.CONSONANT);
            
            for (Phoneme curr : phonemes) {
                if (curr.isUnknown) {
                    output.add(new PhonemeOutput(curr, OutputMode.CONSONANT));
                    state = CVCState.NONE;
                    continue;
                }
                
                if (state == CVCState.NONE) {
                    PhonemeOutput nextOutput = new PhonemeOutput(curr, OutputMode.CONSONANT);
                    output.add(nextOutput);
                    state = curr.isNumeric ? CVCState.NONE : curr.isVowel ? CVCState.VOWEL : CVCState.CONSONANT;
                    prev = nextOutput;
                } else if (state == CVCState.CONSONANT) {
                    if (curr.isVowel) {
                        PhonemeOutput next = new PhonemeOutput(curr, OutputMode.VOWEL);
                        output.add(next);
                        state = CVCState.VOWEL;
                        if (prev.outputMode == OutputMode.LONSUM) {
                            prev.outputMode = OutputMode.CONSONANT;
                        }
                        prev = next;
                    } else {
                        if (curr.phoneme.equals("ng")) {
                            PhonemeOutput next = new PhonemeOutput(curr, OutputMode.VOWEL);
                            output.add(next);
                            state = CVCState.VOWEL;
                            prev = next;
                        } else {
                            if (MAPPER.isApunMayekPhonemesCombo(prev.phoneme, curr)) {
                                output.add(new PhonemeOutput(APUN_MAYEK_AS_PHONEME, OutputMode.CONSONANT));
                            }
                            PhonemeOutput next = new PhonemeOutput(curr,
                                curr.canBeLonsum && prev.outputMode != OutputMode.LONSUM ? OutputMode.LONSUM : OutputMode.CONSONANT
                            );
                            output.add(next);
                            state = CVCState.CONSONANT;
                            prev = next;
                        }
                    }
                } else {
                    if (curr.isVowel) {
                        PhonemeOutput next = new PhonemeOutput(curr, OutputMode.CONSONANT);
                        output.add(next);
                        state = CVCState.CONSONANT;
                        prev = next;
                    } else {
                        PhonemeOutput next = new PhonemeOutput(curr, curr.canBeLonsum ? OutputMode.LONSUM : OutputMode.CONSONANT);
                        output.add(next);
                        state = CVCState.CONSONANT;
                        prev = next;
                    }
                }
            }
            
            StringBuilder text = new StringBuilder();
            for (PhonemeOutput next : output) {
                text.append(next.getOutput());
            }
            return text.toString();
        }
    }

     private static class MeiteiMayekReverseTransliterator {
        private final Set<String> vowels = new HashSet<>(Arrays.asList("a", "e", "i", "o", "u"));
        private final Map<String, String> reverseMeiteiMayekMapping = new HashMap<>();
        private final Map<String, String> reverseMeiteiMayekNumbers = new HashMap<>();
        private final String APUN_MAYEK = "\uABED";

        MeiteiMayekReverseTransliterator() {
            for (String[] phoneme : MEITEI_MAYEK_PHONEMES) {
                if (!phoneme[3].isEmpty()) reverseMeiteiMayekMapping.put(phoneme[3], phoneme[0]);
                if (!phoneme[2].isEmpty()) reverseMeiteiMayekMapping.put(phoneme[2], phoneme[0]);
                if (!phoneme[5].isEmpty()) reverseMeiteiMayekMapping.put(phoneme[5], phoneme[0]);
            }
            for (Map.Entry<String, String> entry : MEITEI_MAYEK_NUMBERS.entrySet()) {
                reverseMeiteiMayekNumbers.put(entry.getKey(), entry.getValue());
            }
        }

        String reverseTransliterate(String text) {
            StringBuilder result = new StringBuilder();
            boolean lastWasConsonant = false;
            boolean skipNext = false;

            for (int i = 0; i < text.length(); i++) {
                if (skipNext) {
                    skipNext = false;
                    continue;
                }

                String character = String.valueOf(text.charAt(i));
                String nextChar = i + 1 < text.length() ? String.valueOf(text.charAt(i + 1)) : "";
                String nextNextChar = i + 2 < text.length() ? String.valueOf(text.charAt(i + 2)) : "";

                if (reverseMeiteiMayekNumbers.containsKey(character)) {
                    result.append(reverseMeiteiMayekNumbers.get(character));
                    lastWasConsonant = false;
                    continue;
                }

                if (reverseMeiteiMayekMapping.containsKey(character)) {
                    String phoneme = reverseMeiteiMayekMapping.get(character);

                    if (isLonsum(character)) {
                        result.append(phoneme);
                        lastWasConsonant = true;
                    } else if (isVowel(phoneme)) {
                        if (phoneme.equals("a") && !result.toString().isEmpty() && !lastWasConsonant) {
                            // Skip 'a' if it's not the first character and follows a vowel
                            continue;
                        }
                        result.append(phoneme);
                        lastWasConsonant = false;
                    } else {
                        result.append(phoneme);
                        lastWasConsonant = true;

                        // Handle Apun Mayek
                        if (nextChar.equals(APUN_MAYEK) && !nextNextChar.isEmpty() && reverseMeiteiMayekMapping.containsKey(nextNextChar)) {
                            String nextPhoneme = reverseMeiteiMayekMapping.get(nextNextChar);
                            result.append(nextPhoneme);
                            skipNext = true;
                            i++; // Skip the Apun Mayek character
                        } else if (!nextChar.isEmpty() && reverseMeiteiMayekMapping.containsKey(nextChar) && isVowelDiacritic(nextChar)) {
                            String vowelPhoneme = reverseMeiteiMayekMapping.get(nextChar);
                            result.append(vowelPhoneme);
                            i++;
                        } else if (i == text.length() - 1) {
                            // If it's the last consonant, add 'a'
                            result.append("a");
                        }
                    }
                    continue;
                }

                result.append(character);
                lastWasConsonant = false;
            }

            return postProcessTransliteration(result.toString());
        }

        private boolean isLonsum(String character) {
            for (String[] phoneme : MEITEI_MAYEK_PHONEMES) {
                if (phoneme[5].equals(character)) return true;
            }
            return false;
        }

        private boolean isVowelDiacritic(String character) {
            for (String[] phoneme : MEITEI_MAYEK_PHONEMES) {
                if (phoneme[2].equals(character) && Boolean.parseBoolean(phoneme[1])) return true;
            }
            return false;
        }

        private boolean isVowel(String phoneme) {
            return vowels.contains(phoneme);
        }

        private String postProcessTransliteration(String text) {
            return text.replaceAll("aa", "a")
                       .replaceAll("ii", "i")
                       .replaceAll("uu", "u")
                       .replaceAll("ee", "e")
                       .replaceAll("oo", "o")
                       .replaceAll("nga", "ng")
                       .replaceAll("(.)\\1+", "$1");
        }
    }


    private final MeiteiMayekTransliterator transliterator = new MeiteiMayekTransliterator();
    private final MeiteiMayekReverseTransliterator reverseTransliterator = new MeiteiMayekReverseTransliterator();

    @SimpleFunction(description = "Transliterate Latin text to Meitei Mayek")
    public String TransliterateToMeiteiMayek(String inputText) {
        return transliterator.transliterate(inputText);
    }

    @SimpleFunction(description = "Transliterate Meitei Mayek text to Latin")
    public String TransliterateToLatin(String inputText) {
        return reverseTransliterator.reverseTransliterate(inputText);
    }
}
