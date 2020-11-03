package org.itron.itrain.utils.DeEnCoder;

import com.google.crypto.tink.KeyManager;
import com.google.crypto.tink.proto.KeyData;
import com.google.protobuf.ByteString;
import com.google.protobuf.MessageLite;

import java.security.GeneralSecurityException;

public class MyAeadKeyManager implements KeyManager<Object> {
    @Override
    public Object getPrimitive(ByteString serializedKey) throws GeneralSecurityException {
        return null;
    }

    @Override
    public Object getPrimitive(MessageLite key) throws GeneralSecurityException {
        return null;
    }

    @Override
    public MessageLite newKey(ByteString serializedKeyFormat) throws GeneralSecurityException {
        return null;
    }

    @Override
    public MessageLite newKey(MessageLite keyFormat) throws GeneralSecurityException {
        return null;
    }

    @Override
    public boolean doesSupport(String typeUrl) {
        return false;
    }

    @Override
    public String getKeyType() {
        return null;
    }

    @Override
    public int getVersion() {
        return 0;
    }

    @Override
    public KeyData newKeyData(ByteString serializedKeyFormat) throws GeneralSecurityException {
        return null;
    }
}
