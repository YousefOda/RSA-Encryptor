import java.io.*;
import java.util.*;

public class RSADecryptor
{
    public static void main(String args[]) throws IOException
    {
        //Scan the encrypted file to get the keys and hidden message
        Scanner in = new Scanner(new File("Encrypted"));

        long d = Integer.parseInt(in.next());
        long n = Integer.parseInt(in.next());
        long C = Integer.parseInt(in.next());

        System.out.println(d);
        System.out.println(n);
        System.out.println(C);

        //Now, solve for R, which is R^d % n
        //Again, same reason why we can't just do Math.pow,
        //the number just gets too big and so we'de lose data
        long R = getR(C, d, n);
        System.out.println(R);

        //Now let's go ahead and put this decrypted message into a file!
        FileWriter fw = new FileWriter("Decrypted");
	    PrintWriter pw = new PrintWriter(fw);

        pw.println(R);

        pw.close();
        fw.close();
        in.close();
    }

    //Loop to get R
    public static long getR(long C, long d, long n)
    {
        long base = C, power = d, newN = n;
        long R = 1;

        //Running loop while the power > 0
        while (power != 0) 
        {
            R = ((R * base) % newN);
            //Power will get reduced after each multiplication
            power--;
        }

        return R;
    }  
}