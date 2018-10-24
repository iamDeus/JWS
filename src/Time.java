import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/*
 *
 * @author Amadeusz Misiak
 */
public abstract class Time {
    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("<dd-MM-yyyy HH:mm:ss> ");
    
    /**
     * Time management method 
     * 
     * @return LocalDateTime object -> local date and time
     */
    public static String currentTime(){ //may be removed if imported from SJWS
        LocalDateTime currentTime = LocalDateTime.now();
        return currentTime.format(FORMATTER);
    }

}
