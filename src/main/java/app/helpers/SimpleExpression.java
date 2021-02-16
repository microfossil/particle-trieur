/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.app.helpers;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Ross Marchant <ross.g.marchant@gmail.com>
 */
public class SimpleExpression {
    
    public final String skippable = "[\\p{L}0-9,;\\[\\\\\\^\\$\\.\\|\\?\\*\\+\\(\\)\\{\\}\\_\\-\\~\\ ]";
    
    private String simpleRegex;
    public String getSimpleRegex() {
        return simpleRegex;
    }
    public void setSimpleRegex(String simpleRegex) {
        this.simpleRegex = simpleRegex;
    }
    
    public SimpleExpression() {
        
    }
    
    public boolean canMatch(String input) {
        try {
            Pattern pattern = Pattern.compile(getComplexRegex());
            Matcher matcher = pattern.matcher(input);
            return matcher.matches();
        }
        catch (Exception ex) {
            return false;
        }
    }

    public HashMap<String,String> parse(String input, String[] parameters) {
        
        HashMap<String,String> parts = new HashMap<>();
        Pattern pattern = Pattern.compile(getComplexRegex());
        
        //Parts
        Matcher matcher = pattern.matcher(input);
        //System.out.print("\n");
        if (matcher.find()) {
            parts.put("matched", matcher.group(0));
            if (matcher.groupCount() > 0) {
                String partsStr = "";
                for (String id : parameters) {
                    try {
                        String part = matcher.group(id);
                        parts.put(id, part);
                        //System.out.print(part + " ");
                    } catch (Exception ex) {
                        //System.out.print(ex.getLocalizedMessage());
                    }
                }
            }
        }
        return parts;
    }
    
    public String getComplexRegex() {

        String input = getSimpleRegex();
        StringBuilder sb = new StringBuilder();

        String[] parts = input.split("\\$");
        int len = parts.length;

        for (int i = 1; i < len; i += 2) {
            String param = parts[i];
            String[] paramParts = param.split(":");
            if (paramParts.length < 2) {
                if (parts[i].equals("skip")) {
                    sb.append("(");
                    sb.append(skippable);
                    sb.append("+)");
                } 
                else if (parts[i].equals("end")) {
                    sb.append("(");
                    sb.append(skippable);
                    sb.append("+)(.*)");
                } 
                else {
                    sb.append("(?<");
                    sb.append(paramParts[0]);
                    sb.append(">");
                    sb.append(skippable);
                    sb.append("+)");
                }
            } 
            else {
                if (parts[i].equals("skip")) {
                    sb.append("(");
                    sb.append(skippable);
                    sb.append("{");
                    sb.append(paramParts[1]);
                    sb.append("})");
                } 
                else {
                    sb.append("(?<");
                    sb.append(paramParts[0]);
                    sb.append(">");
                    sb.append(skippable);
                    sb.append("{");
                    sb.append(paramParts[1]);
                    sb.append("})");
                }
            }
            if (i + 1 < len) {
                sb.append("\\Q" + parts[i + 1] + "\\E");
            }
        }
        return sb.toString();
    }
}
