package com.example.ahn.waitingsystem3;


import java.util.ArrayList;

import javax.xml.transform.URIResolver;

import android.nfc.NdefMessage;
import android.nfc.NdefRecord;

public class NdefMessageParser {

    private NdefMessageParser() {

    }

    public static ArrayList<ParsedRecord> parse(NdefMessage message) {
        return getRecords(message.getRecords());
    }

    public static ArrayList<ParsedRecord> getRecords(NdefRecord[] records) {
        ArrayList<ParsedRecord> elements = new ArrayList<ParsedRecord>();
        for (NdefRecord record : records) {
            /**
             * url인지 text인지 판단하는 조건문을 걸어준다.
             */
            if (UriRecord.isUri(record)) {
                // url이면 elements에 record를 추가해준다.
                elements.add(UriRecord.parse(record));
                // text이면 elements에 record를 추가해준다.
            } else if (TextRecord.isText(record)) {
                elements.add(TextRecord.parse(record));
            }
        }
        return elements;
    }
}