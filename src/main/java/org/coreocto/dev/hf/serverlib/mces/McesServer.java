package org.coreocto.dev.hf.serverlib.mces;

import org.coreocto.dev.hf.commonlib.crypto.IByteCipher;
import org.coreocto.dev.hf.commonlib.sse.mces.CT;
import org.coreocto.dev.hf.commonlib.sse.mces.KeyCipher;
import org.coreocto.dev.hf.commonlib.util.IBase64;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class McesServer {

    private CT cipherText = null;

    private IBase64 base64;

    public McesServer(IBase64 base64) {
        this.base64 = base64;
    }

    public void receive(CT cipherText) {
        //store to server
        this.cipherText = cipherText;
    }

    public String Query2(List<String> queryObj, KeyCipher keyCipher) throws NoSuchPaddingException, InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {

        //queryObj = {root,T1,...,Tm}

        String root = queryObj.get(0);

        List<String> Tis = queryObj.subList(1, queryObj.size());

        Map<String, List<String>> D = cipherText.getD();

        List<String> iniVars = new ArrayList<>(D.get(root));    //initialize variables to equal D[root]

        int m = Tis.size();

        for (int i = 0; i < m; i++) {

            String Ti = Tis.get(i);
            byte[] Ti_in_bytes = base64.decodeToByteArray(Ti);

            int d = iniVars.size() - 1; //skip W at the end of the list

            for (int j = 0; j < d; j++) {

                String f2 = iniVars.get(j);
                byte[] f2_in_bytes = base64.decodeToByteArray(f2);

                boolean canDecrypt = true;
                String f1 = null;

                try {
                    IByteCipher f2Cipher = keyCipher.getByteCipher();
                    byte[] f1_bytes = f2Cipher.decrypt(Ti_in_bytes, f2_in_bytes, new byte[16]);
                    f1 = base64.encodeToString(f1_bytes);
                } catch (BadPaddingException ex) {
                    //sometime when different keys are used for encryption/decryption
                    //it throws a BadPaddingException
                    canDecrypt = false;
                }

                System.out.println(Ti+" = "+f2+" + "+f1);

                List<String> tmp = D.get(f1);

                if (tmp != null) {
                    //update the "variables" if a match is found
                    iniVars.clear();
                    iniVars.addAll(tmp);
                    break;
                }
            }
        }

        return iniVars.get(iniVars.size() - 1); //this is the W
    }

    public List<String> Query4(List<String> x) {
        List<String> result = new ArrayList<>();
        Map<String, String> C = cipherText.getC();
        for (int i=0;i<x.size();i++){
            result.add(C.get(x.get(i)));
        }
        return result;
//        List<String> copy_of_C = new ArrayList<>(cipherText.getC());
//        int cLen = copy_of_C.size();
//
//        for (int i = 0; i < cLen; i++) {
//            int newIdx = x.get(i);
//            copy_of_C.set(i, cipherText.getC().get(newIdx));
//        }
//        return copy_of_C;
    }

    public List<String> Query6(List<String> y) {

        List<String> L = new ArrayList<>();

        int yLen = y.size();
        for (int i=0;i<yLen;i++){
            L.add(this.cipherText.getL().get(y.get(i)));
        }

        return L;
    }
}

