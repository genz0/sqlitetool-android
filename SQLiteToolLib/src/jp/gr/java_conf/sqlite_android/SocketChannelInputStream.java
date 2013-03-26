/*
 * Copyright (C) 2013 genzo
 */
package jp.gr.java_conf.sqlite_android;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * チャネルインプットストリーム.
 * 
 * SocketChannelをInputStreamで読み取る。
 */
public class SocketChannelInputStream extends InputStream {

    /** ソースとなるチャネル. */
    private final SocketChannel mSourceChannel;

    /**
     * コンストラクタ.
     * 
     * SocketChannelを指定下コンストラクタ.
     * 
     * @param channel
     *            SocketChannel
     */
    public SocketChannelInputStream(SocketChannel channel) {
        mSourceChannel = channel;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.io.InputStream#read()
     */
    @Override
    public int read() throws IOException {
        byte[] buf = new byte[1];
        int readLen = read(buf);
        return readLen > 0 ? buf[0] : -1;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.io.InputStream#read(byte[], int, int)
     */
    @Override
    public int read(byte[] buffer, int offset, int length) throws IOException {
        ByteBuffer buf = ByteBuffer.allocate(length);
        int readLen = mSourceChannel.read(buf);
        if (readLen > 0) {
            buf.flip();
            buf.get(buffer, offset, readLen);
        }

        return readLen;
    }

    /*
     * (非 Javadoc)
     * 
     * @see java.io.OutputStream#close()
     */
    @Override
    public void close() throws IOException {
        super.close();
        // 何もしない
    }

}
