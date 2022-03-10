package com.matus.elements.language;

public class RegexTest {

    public String str;
    public String regexNameToRun;
    public boolean isValid;

    public RegexTest(String str, String regexNameToRun) {
        this.str = str;
        this.regexNameToRun = regexNameToRun;
        this.isValid = false; // by default
    }
}
