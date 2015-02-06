package com.ryebrye.releaser.weathersensors;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Created by ddcryang on 2/4/15.
 * <p/>
 * Some code was taken from the RaspberryPI project
 * https://code.google.com/p/raspberry-pi4j-samples/
 * and re-used under the MIT license.
 *
 */
@Profile("raspberryPi")
@Component
public class BMP180Sensor implements TemperatureSensor, BarometricPressureSensor {
    private static final Logger log = LoggerFactory.getLogger(BMP180Sensor.class);
    /*
    Prompt> sudo i2cdetect -y 1
         0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f
    00:          -- -- -- -- -- -- -- -- -- -- -- -- --
    10: -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --
    20: -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --
    30: -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --
    40: -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --
    50: -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --
    60: -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --
    70: -- -- -- -- -- -- -- 77
     */
    // This next addresses is returned by "sudo i2cdetect -y 1", see above.
    public final static int BMP180_ADDRESS = 0x77;
    // set to false if for some reason the BMP180 is not big_endian
    private static final boolean BIG_ENDIAN = true;

    private boolean verbose = false;
    private I2CBus bus;
    private I2CDevice bmp180;

    // conversion factor 0.0002952998751 × Pa = inHg
    public final static double PA_TO_IN_HG_CONVERSION_FACTOR = 0.0002952998751;

    // highest precision mode
    private OperatingMode mode = OperatingMode.ULTRAHIGHRES;

    // BMP180 Registers
    public final static int BMP180_CAL_AC1 = 0xAA;  // R   Calibration data (16 bits)
    public final static int BMP180_CAL_AC2 = 0xAC;  // R   Calibration data (16 bits)
    public final static int BMP180_CAL_AC3 = 0xAE;  // R   Calibration data (16 bits)
    public final static int BMP180_CAL_AC4 = 0xB0;  // R   Calibration data (16 bits)
    public final static int BMP180_CAL_AC5 = 0xB2;  // R   Calibration data (16 bits)
    public final static int BMP180_CAL_AC6 = 0xB4;  // R   Calibration data (16 bits)
    public final static int BMP180_CAL_B1 = 0xB6;  // R   Calibration data (16 bits)
    public final static int BMP180_CAL_B2 = 0xB8;  // R   Calibration data (16 bits)
    public final static int BMP180_CAL_MB = 0xBA;  // R   Calibration data (16 bits)
    public final static int BMP180_CAL_MC = 0xBC;  // R   Calibration data (16 bits)
    public final static int BMP180_CAL_MD = 0xBE;  // R   Calibration data (16 bits)
    public final static int BMP180_CONTROL = 0xF4;
    public final static int BMP180_TEMPDATA = 0xF6;
    public final static int BMP180_PRESSUREDATA = 0xF6;
    public final static int BMP180_READTEMPCMD = 0x2E;
    public final static int BMP180_READPRESSURECMD = 0x34;

    // calibration values that are read before doing anything else
    private final int cal_AC1;
    private final int cal_AC2;
    private final int cal_AC3;
    private final int cal_AC4;
    private final int cal_AC5;
    private final int cal_AC6;
    private final int cal_B1;
    private final int cal_B2;
    private final int cal_MB;
    private final int cal_MC;
    private final int cal_MD;

    public BMP180Sensor() {
        try {
            // Get i2c bus
            bus = I2CFactory.getInstance(I2CBus.BUS_1);
            log.debug("Connected to bus. OK.");

            // Get device itself
            bmp180 = bus.getDevice(BMP180_ADDRESS);
            log.debug("Connected to device. OK.");
        } catch (IOException e) {
            log.error("Couldn't connect to the BMP180 device ");
            throw new RuntimeException("Unable to connect to BMP180", e);
        }

        cal_AC1 = readS16(BMP180_CAL_AC1);   // INT16
        cal_AC2 = readS16(BMP180_CAL_AC2);   // INT16
        cal_AC3 = readS16(BMP180_CAL_AC3);   // INT16
        cal_AC4 = readU16(BMP180_CAL_AC4);   // UINT16
        cal_AC5 = readU16(BMP180_CAL_AC5);   // UINT16
        cal_AC6 = readU16(BMP180_CAL_AC6);   // UINT16
        cal_B1 = readS16(BMP180_CAL_B1);    // INT16
        cal_B2 = readS16(BMP180_CAL_B2);    // INT16
        cal_MB = readS16(BMP180_CAL_MB);    // INT16
        cal_MC = readS16(BMP180_CAL_MC);    // INT16
        cal_MD = readS16(BMP180_CAL_MD);    // INT16
        log.debug("BMP180 Calibration data: AC1: {}, AC2: {}, AC3: {}, AC4: {}, AC5: {}, AC6: {}, B1: {}, B2: {}, MB: {}, MC: {}, MD: {}",
                cal_AC1, cal_AC2, cal_AC3, cal_AC4, cal_AC5, cal_AC6, cal_B1, cal_B2, cal_MB, cal_MC, cal_MD);

    }

    protected static void waitfor(long howMuch) {
        try {
            Thread.sleep(howMuch);
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
    }

    public int readRawTemp() throws Exception {
        // "Reads the raw (uncompensated) temperature from the sensor"
        bmp180.write(BMP180_CONTROL, (byte) BMP180_READTEMPCMD);
        waitfor(5);  // Wait 5ms
        int raw = readU16(BMP180_TEMPDATA);
        log.debug("DBG: Raw Temp: " + (raw & 0xFFFF) + ", " + raw);
        return raw;
    }

    public int readRawPressure() throws Exception {
        // "Reads the raw (uncompensated) pressure level from the sensor"
        bmp180.write(BMP180_CONTROL, (byte) (BMP180_READPRESSURECMD + (this.mode.value() << 6)));
        waitfor(this.mode.getPressureWaitMillis());
        int msb = bmp180.read(BMP180_PRESSUREDATA);
        int lsb = bmp180.read(BMP180_PRESSUREDATA + 1);
        int xlsb = bmp180.read(BMP180_PRESSUREDATA + 2);
        int raw = ((msb << 16) + (lsb << 8) + xlsb) >> (8 - this.mode.value());
        log.debug("DBG: Raw Pressure: " + (raw & 0xFFFF) + ", " + raw);
        return raw;
    }

    public double celciusToFarenheit(double celcius) {
        return ((40 + celcius) * 1.8) - 40;
    }

    @Override
    public double readTemperature() {
        // "Gets the compensated temperature in degrees celcius"
        int UT = 0;
        int X1 = 0;
        int X2 = 0;
        int B5 = 0;
        double temp = 0.0f;

        // Read raw temp before aligning it with the calibration values
        try {
            UT = this.readRawTemp();
        } catch (Exception e) {
            throw new RuntimeException("error reading raw temp", e);
        }
        X1 = ((UT - this.cal_AC6) * this.cal_AC5) >> 15;
        X2 = (this.cal_MC << 11) / (X1 + this.cal_MD);
        B5 = X1 + X2;
        temp = ((B5 + 8) >> 4) / 10.0f;
        if (verbose) {
            System.out.println("DBG: Calibrated temperature = " + temp + " C");
        }
        return temp;
    }

    @Override
    public double readPressureAsInHg() {
        try {
            return readPressureAsPa() * PA_TO_IN_HG_CONVERSION_FACTOR;
        }
        catch(Exception e) {
            throw new RuntimeException("Couldn't read pressure");
        }
    }

    //
    public double readPressureAsPa() throws Exception {
        // "Gets the compensated pressure in pascal"
        int UT = 0;
        int UP = 0;
        int B3 = 0;
        int B5 = 0;
        int B6 = 0;
        int X1 = 0;
        int X2 = 0;
        int X3 = 0;
        int p = 0;
        int B4 = 0;
        int B7 = 0;

        UT = this.readRawTemp();
        UP = this.readRawPressure();

        // True Temperature Calculations
        X1 = (int) ((UT - this.cal_AC6) * this.cal_AC5) >> 15;
        X2 = (this.cal_MC << 11) / (X1 + this.cal_MD);
        B5 = X1 + X2;
        if (log.isTraceEnabled()) {
            log.trace("X1 = " + X1);
            log.trace("X2 = " + X2);
            log.trace("B5 = " + B5);
            log.trace("True Temperature = " + (((B5 + 8) >> 4) / 10.0) + " C");
        }
        // Pressure Calculations
        B6 = B5 - 4000;
        X1 = (this.cal_B2 * (B6 * B6) >> 12) >> 11;
        X2 = (this.cal_AC2 * B6) >> 11;
        X3 = X1 + X2;
        B3 = (((this.cal_AC1 * 4 + X3) << this.mode.value()) + 2) / 4;
        if (log.isTraceEnabled()) {
            log.trace("B6 = " + B6);
            log.trace("X1 = " + X1);
            log.trace("X2 = " + X2);
            log.trace("X3 = " + X3);
            log.trace("B3 = " + B3);
        }
        X1 = (this.cal_AC3 * B6) >> 13;
        X2 = (this.cal_B1 * ((B6 * B6) >> 12)) >> 16;
        X3 = ((X1 + X2) + 2) >> 2;
        B4 = (this.cal_AC4 * (X3 + 32768)) >> 15;
        B7 = (UP - B3) * (50000 >> this.mode.value());
        if (log.isTraceEnabled()) {
            log.trace("X1 = " + X1);
            log.trace("X2 = " + X2);
            log.trace("X3 = " + X3);
            log.trace("B4 = " + B4);
            log.trace("B7 = " + B7);
        }
        if (B7 < 0x80000000) {
            p = (B7 * 2) / B4;
        } else {
            p = (B7 / B4) * 2;
        }

        if (verbose) {
            System.out.println("DBG: X1 = " + X1);
        }

        X1 = (p >> 8) * (p >> 8);
        X1 = (X1 * 3038) >> 16;
        X2 = (-7357 * p) >> 16;
        if (log.isTraceEnabled()) {
            log.trace("p  = " + p);
            log.trace("X1 = " + X1);
            log.trace("X2 = " + X2);
        }
        p = p + ((X1 + X2 + 3791) >> 4);
        if (log.isTraceEnabled()) {
            log.trace("Pressure = " + p + " Pa");
        }

        return p;
    }

    /**
     * Represent the various operating modes that the BMP180 has available
     * <p/>
     * see the data sheet for more information.
     */
    public enum OperatingMode {
        ULTRALOWPOWER(0, 5),
        STANDARD(1, 8),
        HIGHRES(2, 14),
        ULTRAHIGHRES(3, 26);

        private int mode;
        private int pressureWaitMillis;

        OperatingMode(int mode, int pressureWaitMillis) {
            this.mode = mode;
            this.pressureWaitMillis = pressureWaitMillis;
        }

        public int value() {
            return this.mode;
        }

        public int getPressureWaitMillis() {
            return pressureWaitMillis;
        }
    }

    private int readU8(int reg) {
        // "Read an unsigned byte from the I2C device"
        int result = 0;
        try {
            result = this.bmp180.read(reg);
            log.trace("I2C: Device {} returned {} from reg {}", BMP180_ADDRESS, result, reg);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        return result;
    }

    private int readS8(int reg) {
        // "Reads a signed byte from the I2C device"
        int result = 0;
        try {
            result = this.bmp180.read(reg);
            if (result > 127) {
                result -= 256;
            }
            log.trace("I2C: Device {} returned {} from reg {}", BMP180_ADDRESS, result, reg);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        return result;
    }

    private int readU16(int register) {
        try {
            int hi = this.readU8(register);
            int lo = this.readU8(register + 1);
            return (BIG_ENDIAN) ? (hi << 8) + lo : (lo << 8) + hi;
        } catch (Exception e) {
            throw new RuntimeException("error readingU16", e);
        }
    }

    private int readS16(int register) {
        try {
            int hi = 0, lo = 0;
            if (BIG_ENDIAN) {
                hi = this.readS8(register);
                lo = this.readU8(register + 1);
            } else {
                lo = this.readS8(register);
                hi = this.readU8(register + 1);
            }
            return (hi << 8) + lo;
        } catch (Exception e) {
            throw new RuntimeException("error readingS16", e);
        }

    }

}
