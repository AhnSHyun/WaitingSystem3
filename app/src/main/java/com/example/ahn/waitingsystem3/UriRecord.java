package com.example.ahn.waitingsystem3;


import java.nio.charset.Charset;
import java.util.Arrays;
import android.net.Uri;
import android.nfc.NdefRecord;
import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.primitives.Bytes;

public class UriRecord implements ParsedRecord {
    private static final String TAG = "UriRecord";
    public static final String RECORD_TYPE = "UriRecord";

    /**
     * NFC Forum "URI Record Type Definition"
     *
     * This is a mappin of "URI Identifier Codes" to URI string prefixes per
     * section 3.2.2 of the NFC Forum URI Record Type Definition document.
     *
     * */

    /** 그냥 자주쓰는 URL 앞에있는 기호들을 번호를매겨서, 컬렉션클래스에 넣어준다.**/
    private static final BiMap<Byte, String> URI_PREFIX_MAP = ImmutableBiMap
            .<Byte, String> builder().put((byte) 0x00, "")
            .put((byte) 0x01, "http://www.").put((byte) 0x02, "https://www.")
            .put((byte) 0x03, "http://").put((byte) 0x04, "https://")
            .put((byte) 0x05, "tel:").put((byte) 0x06, "mailto:")
            .put((byte) 0x07, "ftp://anonymous:anonymous@")
            .put((byte) 0x08, "ftp://ftp.").put((byte) 0x09, "ftps://")
            .put((byte) 0x0A, "sftp://").put((byte) 0x0B, "smb://")
            .put((byte) 0x0C, "nfs://").put((byte) 0x0D, "ftp://")
            .put((byte) 0x0E, "dav://").put((byte) 0x0F, "news:")
            .put((byte) 0x10, "telnet://").put((byte) 0x11, "imap:")
            .put((byte) 0x12, "rtsp://").put((byte) 0x13, "urn:")
            .put((byte) 0x14, "pop:").put((byte) 0x15, "sip:")
            .put((byte) 0x16, "sips:").put((byte) 0x17, "tftp:")
            .put((byte) 0x18, "btspp://").put((byte) 0x19, "btl2cap://")
            .put((byte) 0x1A, "btgoep://").put((byte) 0x1B, "tcpobex://")
            .put((byte) 0x1C, "irdaobex://").put((byte) 0x1D, "file://")
            .put((byte) 0x1E, "urn:epc:id:").put((byte) 0x1F, "urn:epc:tag:")
            .put((byte) 0x20, "urn:epc:pat:").put((byte) 0x21, "urn:epc:raw:")
            .put((byte) 0x22, "urn:epc:").put((byte) 0x23, "urn:nfc:").build();

    private final Uri mUri;

    private UriRecord(Uri uri) {
        this.mUri = Preconditions.checkNotNull(uri);

    }

    /**

     변수 길이 타입 필드를 해석하는 tnf가 , TNF_ABSOLUTE_URI이므로



     묻지도 따지지도 않고, URL 타입을 가져온다.*/
    private static UriRecord parseAbsolute(NdefRecord record) {
        byte[] payload = record.getPayload();
        Uri uri = Uri.parse(new String(payload, Charset.forName("UTF-8")));
        return new UriRecord(uri);

    }


    /** Parse an well known URI record

     한마디로 TNF_WELL_KNOWN 타입에도 3가지종류가 있다.

     RTD_SMART_POSTER : 페이로드 파싱에 따른 URI,

     RTD_TEXT : text/plain MIME 타입

     RTD_URI : URI 데이터 이렇게 3가지가 있다.

     그래서 TNF_WELL_KNOWN이라고 하면, 그안에서 데이터타입을

     고르겠다는 말이다.

     */
    private static UriRecord parseWellKnown(NdefRecord record) {

        /** 레코드타입이 RTD_URI와 같은지 확인한다. 같지않으면 에러를 던져주어서 해당 메소드를 벗어나게된다. */
        Preconditions.checkArgument(Arrays.equals(record.getType(),
                NdefRecord.RTD_URI));
        /** 같으면, 데이터를 가져온다 */
        byte[] payload = record.getPayload();

        /**
         * URI_PREFIX_MAP에서, payload의 첫번째 인덱스와 같은것을 찾아 온다. : (http://, http://www.) 이런것들...
         */
        String prefix = URI_PREFIX_MAP.get(payload[0]);

        /**
         * prefix의 내용과, 데이터의 1번째 인덱스부터 합친 내용을 fullUri에 저장한다.
         */
        byte[] fullUri = Bytes.concat(
                prefix.getBytes(Charset.forName("UTF-8")),
                Arrays.copyOfRange(payload, 1, payload.length));

/*

두 내용을 UTF-8로 변환해서 uri에 저장한다.

*/
        Uri uri = Uri.parse(new String(fullUri, Charset.forName("UTF-8")));
        return new UriRecord(uri);
    }




    public static UriRecord parse(NdefRecord record) {
        /***
         * record tnf타입을 통해 파싱 방법을 정한다.

         tnf란 :변수 길이 타입 필드를 해석하는 방법을 가리킨다.
         */
        short tnf = record.getTnf();


        if (tnf == NdefRecord.TNF_WELL_KNOWN) {
            // tnf가 TNF_WELL_KNOWN(잘알려진 방식(종류)) 이라면

            return parseWellKnown(record);
        } else if (tnf == NdefRecord.TNF_ABSOLUTE_URI) {
            /**
             * 저게 무엇을 뜻하느냐에 앞서 기본적인것을 알고가겠다.

             * Variable length type: 레코드의 타입을 나타낸준다.

             * 만약 TNF_ABSOLUTE_URI 대신 TNF_WELL_KNOWN을 사용하면 TNF_WELL_KNOWN안에 어떤 레코드 타입을

             * 쓸지 또골라야한다.

             * 한마디로 또 레코드 타입을 명시해줘야한다는것이다.

             * 반면, TNF_ABSOLUTE_URI를 사용하게되면, 자동으로 레코드 타입이 URI로 매핑된다. 그후, 태그에대한

             * 정보와 함께 ACTION_NDEF_DISCOVERED인텐트의 데이터 필드안에 URI를 캡슐화 시킨다.
             */
            return parseAbsolute(record);
        }
        throw new IllegalArgumentException("Unknown TNF" + tnf);

    }

    public static boolean isUri(NdefRecord record) {
        try {
            parse(record);
            return true;
        } catch (IllegalArgumentException e) {
            // TODO: handle exception
            return false;
        }
    }

    @Override
    public int getType() {
        // TODO Auto-generated method stub
        return ParsedRecord.TYPE_URI;
    }

    public Uri getUri() {
        return mUri;
    }

}