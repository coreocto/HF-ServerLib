package org.coreocto.dev.hf.serverlib.suise;

import org.apache.log4j.Logger;
import org.coreocto.dev.hf.commonlib.crypto.IKeyedHashFunc;
import org.coreocto.dev.hf.commonlib.sse.suise.bean.AddTokenResult;
import org.coreocto.dev.hf.commonlib.sse.suise.util.SuiseUtil;
import org.coreocto.dev.hf.commonlib.util.IBase64;

import java.io.File;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class SuiseServer {

    private static final Logger LOGGER = Logger.getLogger(SuiseServer.class);

    private Map<String, List<String>> regularIdx = null;
    private Map<String, List<String>> searchTokenIdx = null;
    private Map<String, File> encFileList = null;
    private IBase64 base64 = null;

    private SuiseUtil suiseUtil = null;

    public boolean isDataProtected() {
        return dataProtected;
    }

    public void setDataProtected(boolean dataProtected) {
        this.dataProtected = dataProtected;
    }

    private boolean dataProtected = true;

    public SuiseServer(SuiseUtil suiseUtil, IBase64 base64) {
        this.regularIdx = new HashMap<String, List<String>>();
        this.searchTokenIdx = new HashMap<String, List<String>>();
        this.encFileList = new HashMap<String, File>();

        this.suiseUtil = suiseUtil;
        this.base64 = base64;
    }

    public void Add(String fileId, File encFile, List<String> c, List<String> x) {

        regularIdx.put(fileId, c);
        for (String xElement : x) {
            List<String> tmpList = searchTokenIdx.get(xElement);
            if (!tmpList.contains(fileId)) {
                tmpList.add(fileId);
            }
        }
        encFileList.put(fileId, encFile);
    }

    public void Add(AddTokenResult addTokenResult, File encFile) {
        String docId = addTokenResult.getId();
        regularIdx.put(docId, addTokenResult.getC());
        for (String xElement : addTokenResult.getX()) {
            List<String> tmpList = searchTokenIdx.get(xElement);
            if (!tmpList.contains(docId)) {
                tmpList.add(docId);
            }
        }
        encFileList.put(docId, encFile);
    }


    public List<String> Search(String searchToken, IKeyedHashFunc keyedHashFunc) throws NoSuchAlgorithmException, InvalidKeyException {

        List<String> iw = null;

        if (searchTokenIdx.containsKey(searchToken)) {
            iw = searchTokenIdx.get(searchToken);
        } else {
            iw = new ArrayList<>();

            for (Map.Entry<String, List<String>> entry : regularIdx.entrySet()) {
                String fileId = entry.getKey();
                List<String> tmpList = entry.getValue();

                if (!dataProtected) {
                    if (tmpList.contains(searchToken)) {
                        iw.add(fileId);
                    }
                } else {

                    int listSize = tmpList.size();

                    byte[] randomBytes = new byte[16];
                    byte[] hashBytes = new byte[16];
                    byte[] srhTknBytes = null;

                    try {
                        srhTknBytes = base64.decodeToByteArray(searchToken);
                    } catch (Exception e) {
                        LOGGER.error("error when decoding searchToken into bytes", e);
                        continue;
                    }

                    for (int i = 0; i < listSize; i++) {
                        String ci = tmpList.get(i);

                        byte[] data = base64.decodeToByteArray(ci);

                        if (data == null || data.length < 32) {
                            continue;
                        } else {

                            //first 16 bytes are the hash value
                            System.arraycopy(data, 0, hashBytes, 0, 16);

                            //last 16 bytes are the random number
                            System.arraycopy(data, 16, randomBytes, 0, 16);

                            byte[] hashedTokenBytes = keyedHashFunc.getHash(srhTknBytes, randomBytes);

                            if (Arrays.equals(hashBytes, hashedTokenBytes)) {
                                iw.add(fileId);
                            }
                        }
                    }
                }
            }
            searchTokenIdx.put(searchToken, iw);
        }

        return iw;
    }


    public void Delete(String fileId) {

        regularIdx.remove(fileId);
        for (Map.Entry<String, List<String>> entry : searchTokenIdx.entrySet()) {
            List<String> val = entry.getValue();
            if (val.contains(fileId)) {
                val.remove(fileId);
            }
        }
        encFileList.remove(fileId);
    }
}
