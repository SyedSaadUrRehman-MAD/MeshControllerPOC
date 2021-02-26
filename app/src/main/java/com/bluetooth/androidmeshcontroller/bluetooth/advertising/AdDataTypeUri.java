package com.bluetooth.androidmeshcontroller.bluetooth.advertising;

import java.io.UnsupportedEncodingException;

/**
 * Created by mwoolley on 29/07/2015.
 */
public class AdDataTypeUri extends AdDataType {

    private String scheme_name="";
    private String uri="";

    private String [] scheme_names = {
            "aaa:",
            "aaas:",
            "about:",
            "acap:",
            "acct:",
            "cap:",
            "cid:",
            "coap:",
            "coaps:",
            "crid:",
            "data:",
            "dav:",
            "dict:",
            "dns:",
            "file:",
            "ftp:",
            "geo:",
            "go:",
            "gopher:",
            "h323:",
            "http:",
            "https:",
            "iax:",
            "icap:",
            "im:",
            "imap:",
            "info:",
            "ipp:",
            "ipps:",
            "iris:",
            "iris.beep:",
            "iris.xpc:",
            "iris.xpcs:",
            "iris.lwz:",
            "jabber:",
            "ldap:",
            "mailto:",
            "mid:",
            "msrp:",
            "msrps:",
            "mtqp:",
            "mupdate:",
            "news:",
            "nfs:",
            "ni:",
            "nih:",
            "nntp:",
            "opaquelocktoken:",
            "pop:",
            "pres:",
            "reload:",
            "rtsp:",
            "rtsps:",
            "rtspu:",
            "service:",
            "session:",
            "shttp:",
            "sieve:",
            "sip:",
            "sips:",
            "sms:",
            "snmp:",
            "soap.beep:",
            "soap.beeps:",
            "stun:",
            "stuns:",
            "tag:",
            "tel:",
            "telnet:",
            "tftp:",
            "thismessage:",
            "tn3270:",
            "tip:",
            "turn:",
            "turns:",
            "tv:",
            "urn:",
            "vemmi:",
            "ws:",
            "wss:",
            "xcon:",
            "xcon-userid:",
            "xmlrpc.beep:",
            "xmlrpc.beeps:",
            "xmpp:",
            "z39.50r:",
            "z39.50s:",
            "acr:",
            "adiumxtra:",
            "afp:",
            "afs:",
            "aim:",
            "apt:",
            "attachment:",
            "aw:",
            "barion:",
            "beshare:",
            "bitcoin:",
            "bolo:",
            "callto:",
            "chrome:",
            "chrome-extension:",
            "com-eventbrite-attendee:",
            "content:",
            "cvs:",
            "dlna-playsingle:",
            "dlna-playcontainer:",
            "dtn:",
            "dvb:",
            "ed2k:",
            "facetime:",
            "feed:",
            "feedready:",
            "finger:",
            "fish:",
            "gg:",
            "git:",
            "gizmoproject:",
            "gtalk:",
            "ham:",
            "hcp:",
            "icon:",
            "ipn:",
            "irc:",
            "irc6:",
            "ircs:",
            "itms:",
            "jar:",
            "jms:",
            "keyparc:",
            "lastfm:",
            "ldaps:",
            "magnet:",
            "maps:",
            "market:",
            "message:",
            "mms:",
            "ms-help:",
            "ms-settings-power:",
            "msnim:",
            "mumble:",
            "mvn:",
            "notes:",
            "oid:",
            "palm:",
            "paparazzi:",
            "pkcs11:",
            "platform:",
            "proxy:",
            "psyc:",
            "query:",
            "res:",
            "resource:",
            "rmi:",
            "rsync:",
            "rtmfp:",
            "rtmp:",
            "secondlife:",
            "sftp:",
            "sgn:",
            "skype:",
            "smb:",
            "smtp:",
            "soldat:",
            "spotify:",
            "ssh:",
            "steam:",
            "submit:",
            "svn:",
            "teamspeak:",
            "teliaeid:",
            "things:",
            "udp:",
            "unreal:",
            "ut2004:",
            "ventrilo:",
            "view-source:",
            "webcal:",
            "wtai:",
            "wyciwyg:",
            "xfire:",
            "xri:",
            "ymsgr:",
            "example:",
            "ms-settings-cloudstorage:"
    };


    public AdDataTypeUri(byte type_id, String type_name, byte[] value_bytes) {
        super(type_id, type_name, value_bytes);
        byte scheme = value_bytes[0];
        if (scheme != 1) {
            int scheme_inx = scheme - 2;
            if (scheme_inx > -1 && scheme_inx < scheme_names.length) {
                scheme_name = scheme_names[scheme_inx];
            } else {
                scheme_name = "????:";
            }
        } else {
            scheme_name = ":";
        }
        byte [] uri_bytes = new byte[value_bytes.length - 1];
        System.arraycopy(value_bytes,1,uri_bytes,0,value_bytes.length-1);
        try {
            uri = new String(uri_bytes,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            uri = "Encoding Error";
        }
    }


    public String toString() {
        String s = super.toString();
        s = s + scheme_name + uri;
        s = s + "\n";
        return s;
    }

    public String getScheme_name() {
        return scheme_name;
    }

    public String getUri() {
        return uri;
    }
}
