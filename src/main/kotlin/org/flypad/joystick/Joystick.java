/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.flypad.joystick;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinNT;
import org.flypad.joystick.jna.IOCTL;
import org.flypad.joystick.jna.JoystickState;
import org.flypad.joystick.jna.TempStruct;

import java.util.Arrays;
import java.util.logging.Logger;

/**
 * @author albus
 */
public class Joystick implements JoystickConstants {
    Logger logger = Logger.getLogger(getClass().getName());

    public static final String DEFAULT_NAME = "\\\\.\\PPJoyIOCTL1";

    /**
     * Analog values (axes)
     */
    public final int[] analog = new int[NUM_ANALOG];

    /**
     * Digital values (buttons)
     */
    public final byte[] digital = new byte[NUM_DIGITAL];

    private final TempStruct temp = new TempStruct();

    private WinNT.HANDLE handle = null;
    private final JoystickState state = new JoystickState();
    private final int size =
            Native.getNativeSize(JoystickState.class, state);

    public Joystick() throws JoystickException {
        this(DEFAULT_NAME);
    }

    public Joystick(final String name) throws JoystickException {
        if (name == null) {
            throw new NullPointerException();
        }
        initializeHandle(name);
        resetButtons();
    }

    private void initializeHandle(final String name) throws JoystickException {
        WinNT.HANDLE h = Kernel32.INSTANCE.CreateFile(
                name,
                Kernel32.GENERIC_WRITE,
                Kernel32.FILE_SHARE_WRITE,
                null,
                Kernel32.OPEN_EXISTING,
                0,
                null);

        final int rc = IOCTL.INSTANCE.GetLastError();

        if (rc != 0) {
            throw new JoystickException(
                    "Cannot create handle. Error code: " + rc);
        }

        handle = h;
        logger.info("Handle created: " + h.toString());
    }

    public final void resetButtons() {
        Arrays.fill(analog, ANALOG_DEFAULT);

        Arrays.fill(digital, DIGITAL_OFF);
    }

    public final synchronized void close() throws JoystickException {
        if (handle == null) {
            throw new JoystickException("Joystick already closed");
        }

        Kernel32.INSTANCE.CloseHandle(handle);

        final int rc = IOCTL.INSTANCE.GetLastError();

        if (rc != 0) {
            throw new JoystickException(
                    "Cannot close handle. Error code: " + rc);
        }
    }

    protected final void finalize() throws Throwable {
        try {
            close();
        } catch (Exception e) {
        }

        super.finalize();
    }

    public final void send() throws JoystickException {
        /*
         * Copy the data to the joystick state
         */
        System.arraycopy(analog, 0, state.analog, 0, NUM_ANALOG);
        System.arraycopy(digital, 0, state.digital, 0, NUM_DIGITAL);
        state.write();

        IOCTL.INSTANCE.DeviceIoControl(
                handle,
                0x220000,
                state,
                size,
                Pointer.NULL,
                0,
                temp.getPointer(),
                null
        );

        final int rc = IOCTL.INSTANCE.GetLastError();

        if (rc != 0) {
            throw new JoystickException(
                    "DeviceIoControl error. Error code: " + rc);
        }
    }
}
