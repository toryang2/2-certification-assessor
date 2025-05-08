/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package assessor.component.report;

import java.util.Arrays;
import java.util.stream.Collectors;
public class TextNormalizer {
        public static String toProperCase(String input) {
        if (input == null || input.trim().isEmpty()) {
            return "";
        }
        
        // Split the input string by spaces, capitalize each word, and join them back.
        return Arrays.stream(input.split("\\s+"))
                .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase())
                .collect(Collectors.joining(" "));
    }
}
