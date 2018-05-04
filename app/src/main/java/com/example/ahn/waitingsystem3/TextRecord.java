package com.example.ahn.waitingsystem3;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import android.nfc.NdefRecord;
import com.google.common.base.Preconditions;
public class TextRecord implements ParsedRecord {

    private final String mLanguageCode;
    private final String mText;

    private TextRecord(String languageCode, String text) {
        mLanguageCode = Preconditions.checkNotNull(languageCode);
        mText = Preconditions.checkNotNull(text);
    }

    @Override
    public int getType() {
        return ParsedRecord.TYPE_TEXT;
    }

    public String getText() {
        return mText;
    }

    /**
     * Returns the ISO/IANA language code associated with this text element.
     */
    public String getLanguageCode() {
        return mLanguageCode;
    }


    public static TextRecord parse(NdefRecord record) {
        Preconditions
                .checkArgument(record.getTnf() == NdefRecord.TNF_WELL_KNOWN);
        Preconditions.checkArgument(Arrays.equals(record.getType(),
                NdefRecord.RTD_TEXT));
        try {
            byte[] payload = record.getPayload();


  /*payload[0]인덱스에는 인코딩 상태가 byte형식으로 포함되어있다,

그래서 해당 정보를 가지고, UTF-8형식으로 byte를 풀지, UTF-16형식으로 풀지 결정한다.*/
            String textEncoding = ((payload[0] & 0200) == 0) ? "UTF-8"
                    : "UTF-16";



            /*0077로 하여금 태그에 어떤 언어가 쓰여져 있는지 판단한다. */
            int languageCodeLength = payload[0] & 0077;



 /*페이로드의 첫번째 인덱스를, US-ASCII형식으로 변환한다.

디버그 결관 languageCode = 항상 en이 찍힌다. */
            String languageCode = new String(payload, 1, languageCodeLength,
                    "US-ASCII");



            /*데이터를 2+1 부터 데이터길이 - 2-1 만큼 textEncoding형식으로 뽑아온다.*/
            String text = new String(payload, languageCodeLength + 1,
                    payload.length - languageCodeLength - 1, textEncoding);
            return new TextRecord(languageCode, text);
        } catch (UnsupportedEncodingException e) {
            // should never happen unless we get a malformed tag.
            throw new IllegalArgumentException(e);
        }
    }



    public static boolean isText(NdefRecord record) {
        try {
            parse(record);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}