package com.matus;

import java.util.*;

public class Utils {


    public static String padRight(String s, int n) {
        return String.format("%-" + n + "s", s);
    }

    public static String padLeft(String s, int n) {
        return String.format("%" + n + "s", s);
    }


    public static String removeStringListDuplicates(String list) {
        ArrayList<String> newList = new ArrayList<>();
        for (String element : list.split(",")) {
            if (!newList.contains(element)) {
                newList.add(element);
            }
        }
        String f = String.join(",", newList);
        return f;
    }

    public static String orderStringArray(String str) {

        String[] arr = str.split(",");
        int[] intArr = new int[arr.length];

        for (int i = 0; i < arr.length; i++) {
            intArr[i] = Integer.parseInt(arr[i]);
        }
        Arrays.sort(intArr);
        for (int i = 0; i < arr.length; i++) {
            arr[i] = Integer.toString(intArr[i]);
        }
        String f = String.join(",", arr);
        return f;
    }

    public static String centerString (String s, int n) {
        return String.format("%-" + n  + "s", String.format("%" + (s.length() + (n - s.length()) / 2) + "s", s));
    }




    public static String arrayToHTMLTable(String title, String[] headers, String[][] dataArray) {
        String output = String.format("<table border=\"1\"><thead><tr><td>%s</td></tr>",title);
        //Row 1 of Titles
        for (String header : headers) {
            output += String.format("<td>%s</td>", header);
        }
        output += "</tr></thead><tbody>";
        //Row 2 of Values
        for(String[] data : dataArray) {
            output += "<tr>";
            for (String datum : data) {
                output += String.format("<td>%s</td>",datum);
            }
            output += "</tr>";
        }
        output += "</tbody></table>";
        return output;
    }





}
