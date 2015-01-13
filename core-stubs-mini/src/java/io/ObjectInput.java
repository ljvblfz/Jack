package java.io;
public interface ObjectInput
  extends java.io.DataInput, java.lang.AutoCloseable
{
public abstract  int available() throws java.io.IOException;
public abstract  void close() throws java.io.IOException;
public abstract  int read() throws java.io.IOException;
public abstract  int read(byte[] buffer) throws java.io.IOException;
public abstract  int read(byte[] buffer, int byteOffset, int byteCount) throws java.io.IOException;
public abstract  java.lang.Object readObject() throws java.lang.ClassNotFoundException, java.io.IOException;
public abstract  long skip(long byteCount) throws java.io.IOException;
}
