package edu.knox.cder.testy;

class ByteArrayClassLoader extends ClassLoader
{

    private final byte[] classFileBytes;

    ByteArrayClassLoader(byte[] classFileBytes)
    {
        this.classFileBytes = classFileBytes;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException
    {
        return defineClass(name, classFileBytes, 0, classFileBytes.length);
    }
}
