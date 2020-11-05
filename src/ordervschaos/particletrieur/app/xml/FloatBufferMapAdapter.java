/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ordervschaos.particletrieur.app.xml;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.Base64;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 *
 * @author rossm
 */
public class FloatBufferMapAdapter extends XmlAdapter<String, float[]> {

    @Override
    public float[] unmarshal(String v) throws Exception {
//        float[] floats = new float[v.length/4];
//        ByteBuffer.wrap(v).asFloatBuffer().get(floats).array();
//        return floats;
        return decodeLocation(v);
    }

    @Override
    public String marshal(float[] v) throws Exception {
        return encodeLocation(v);
//        ByteBuffer buffer = ByteBuffer.allocate(4 * v.length);
//        buffer.asFloatBuffer().put(v,0,v.length);
//        return buffer.array();
    }    
    
    public static String encodeLocation(float[] floatArray) {
        //return Base64.getEncoder().encodeToString(floatToByteArray(floatArray));
        short[] shortArray = new short[floatArray.length];
        for (int i = 0; i < floatArray.length; i++) {
            shortArray[i] = (short) fromFloat(floatArray[i]);
        }
        return Base64.getEncoder().encodeToString(shortToByteArray(shortArray));
    }
    public static float[] decodeLocation(String base64Encoded) {
        if (base64Encoded.length() <= 8192) {
            short[] vals = byteToShortArray(Base64.getDecoder().decode(base64Encoded));
            float[] floatArray = new float[vals.length];
            for (int i = 0; i < vals.length; i++) {
                floatArray[i] = toFloat(vals[i]);
            }
            return floatArray;
        }
        else {
            return byteToFloatArray(Base64.getDecoder().decode(base64Encoded));
        }
    }

    public static byte[] floatToByteArray(float[] floatArray) {
        ByteBuffer buf = ByteBuffer.allocate(Float.SIZE / Byte.SIZE * floatArray.length);
        buf.asFloatBuffer().put(floatArray);
        return buf.array();
    }

    public static float[] byteToFloatArray(byte[] bytes) {
        FloatBuffer buf = ByteBuffer.wrap(bytes).asFloatBuffer();
        float[] floatArray = new float[buf.limit()];
        buf.get(floatArray);
        return floatArray;
    }

    public static byte[] shortToByteArray(short[] shortArray) {
        ByteBuffer buf = ByteBuffer.allocate(Short.SIZE / Byte.SIZE * shortArray.length);
        buf.asShortBuffer().put(shortArray);
        return buf.array();
    }

    public static short[] byteToShortArray(byte[] bytes) {
        ShortBuffer buf = ByteBuffer.wrap(bytes).asShortBuffer();
        short[] shortArray = new short[buf.limit()];
        buf.get(shortArray);
        return shortArray;
    }

    // returns all higher 16 bits as 0 for all results
    public static int fromFloat( float fval )
    {
        int fbits = Float.floatToIntBits( fval );
        int sign = fbits >>> 16 & 0x8000;          // sign only
        int val = ( fbits & 0x7fffffff ) + 0x1000; // rounded value

        if( val >= 0x47800000 )               // might be or become NaN/Inf
        {                                     // avoid Inf due to rounding
            if( ( fbits & 0x7fffffff ) >= 0x47800000 )
            {                                 // is or must become NaN/Inf
                if( val < 0x7f800000 )        // was value but too large
                    return sign | 0x7c00;     // make it +/-Inf
                return sign | 0x7c00 |        // remains +/-Inf or NaN
                        ( fbits & 0x007fffff ) >>> 13; // keep NaN (and Inf) bits
            }
            return sign | 0x7bff;             // unrounded not quite Inf
        }
        if( val >= 0x38800000 )               // remains normalized value
            return sign | val - 0x38000000 >>> 13; // exp - 127 + 15
        if( val < 0x33000000 )                // too small for subnormal
            return sign;                      // becomes +/-0
        val = ( fbits & 0x7fffffff ) >>> 23;  // tmp exp for subnormal calc
        return sign | ( ( fbits & 0x7fffff | 0x800000 ) // add subnormal bit
                + ( 0x800000 >>> val - 102 )     // round depending on cut off
                >>> 126 - val );   // div by 2^(1-(exp-127+15)) and >> 13 | exp=0
    }

    // ignores the higher 16 bits
    public static float toFloat( int hbits )
    {
        int mant = hbits & 0x03ff;            // 10 bits mantissa
        int exp =  hbits & 0x7c00;            // 5 bits exponent
        if( exp == 0x7c00 )                   // NaN/Inf
            exp = 0x3fc00;                    // -> NaN/Inf
        else if( exp != 0 )                   // normalized value
        {
            exp += 0x1c000;                   // exp - 15 + 127
            if( mant == 0 && exp > 0x1c400 )  // smooth transition
                return Float.intBitsToFloat( ( hbits & 0x8000 ) << 16
                        | exp << 13 | 0x3ff );
        }
        else if( mant != 0 )                  // && exp==0 -> subnormal
        {
            exp = 0x1c400;                    // make it normal
            do {
                mant <<= 1;                   // mantissa * 2
                exp -= 0x400;                 // decrease exp by 1
            } while( ( mant & 0x400 ) == 0 ); // while not normal
            mant &= 0x3ff;                    // discard subnormal bit
        }                                     // else +/-0 -> +/-0
        return Float.intBitsToFloat(          // combine all parts
                ( hbits & 0x8000 ) << 16          // sign  << ( 31 - 15 )
                        | ( exp | mant ) << 13 );         // value << ( 23 - 10 )
    }
}
