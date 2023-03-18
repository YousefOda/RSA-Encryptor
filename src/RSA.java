import java.util.*;
import java.io.*;
import java.math.*;

public class RSA
{
    public RSA()
    {
    }

    // Takes an array of characters and encrypts it
    public void encrypt(char[] input) throws Exception 
    {
        // Produces a "table" of primes
        ArrayList<Long> lop = listOfPrimes();
        Scanner in = new Scanner(System.in);

        // Choose LARGE, DISTINCT primes p,q
        Random rand = new Random();
        long p = lop.get(rand.nextInt(0, lop.size()));
        long q = 0;

        // This loop is to check if q is distinct from p
        while(true)
        {
            q = lop.get(rand.nextInt(0, lop.size()));

            if(p != q)
            {
                break;
            }
        }

        // n = pq and (p-1)(q-1)
        long n = p * q;
        long p1q1 = (p-1)*(q-1);

        System.out.println("p = " + p);
        System.out.println("q = " + q);
        System.out.println("n = " + n);
        System.out.println("(p-1)(q-1) = " + p1q1);

        // Choose e,d such that ed = 1 (mod (p-1)(q-1)) and
        // 1 < e,d < (p-1)(q-1). Also, gcd(e, (p-1)(q-1)) = 1 (Important for LCT/LDE)
        // After looking around, apparently, 65537 is the most commonly
        // used e value for RSA encryption. You learn something new everyday.
        long e = 65537;
        System.out.println("e = " + e);

        // Now, we need d by solving an LDE/LCT
        // To do this, we use EEA
        long d = getD(e, p1q1);
        System.out.println("d = " + d);
        System.out.println("ed (mod " + p1q1 + ") = " + ((e*d) % p1q1));

        // Now we can take the charArray input and do M^e = C, and put each character into the file
        char[] cinput = input;
        FileWriter fw = new FileWriter("Encrypted");
	    PrintWriter pw = new PrintWriter(fw);
        long M = 0;

        // Let's first put in the decryption key
        pw.println(d);
        pw.println(n);

        // Then, we encrypt each character
        // Note that in getCorR, we cannot just do Math.pow, as the
        // number would be so large it would overflow. In a previous
        // implementation, I used a while loop to counter this, however, 
        // at larger values, this would have been INCREDIBLY inefficient.
        for(int i = 0; i < cinput.length; i++)
        {
            M = (long)cinput[i];
            pw.println(getCorR(M, e, n));
        }
    
        // In an actual application of RSA, what would happen is that the
        // public and private keys are made by A, the public key is given to B
        // so that they can encrypt their message, and then B sends the ciphertext
        // to A, since ONLY A has the private key. We send the public key, as it
        // is ONLY used for encryption, NOT decryption, so Eavesdroppers cannot
        // parse out the message

        // Public Key (e,n)
        // Private Key (d,n)

        System.out.println("Done!");

        pw.close();
        fw.close();
        in.close();
    }


    // Takes a file and decrypts it
    public void decrypt(String file) throws IOException
    {
        // Scan the encrypted file to get the keys and hidden message
        Scanner in = new Scanner(new File(file));

        long d = Long.parseLong(in.next());
        long n = Long.parseLong(in.next());

        System.out.println(d);
        System.out.println(n);

        // Now the rest of the file contains the encrypted message
        // Now, solve for R, which is C^d % n
        // Again, same reason why we can't just do Math.pow,
        // the number just gets too big and so we would lose data

        // Here, we will loop through the file, and put the output into a file
        FileWriter fw = new FileWriter("Decrypted");
	    PrintWriter pw = new PrintWriter(fw);
        long C = 0;
        String OM = "";

        while(in.hasNext())
        {
            C = in.nextLong();
            long R = getCorR(C, d, n);
            System.out.println((char)R);
            OM = OM + (char)R;
        }

        pw.println(OM);

        System.out.println("Done!");

        pw.close();
        fw.close();
        in.close();
    }

    // This creates a "table" of primes that we can randomly choose
    public static ArrayList<Long> listOfPrimes()
    {
        ArrayList<Long> lop = new ArrayList<Long>();

        // We can change these bounds to have larger or smaller primes
        for(long i = 1000000; i < 9999999; i++)
        {
            if(isPrime(i))
            {
                lop.add(i);
            }
        }

        return lop;
    }

    // This helps us determine if a number is prime
    // I believe this is O(nlog(n))
    public static boolean isPrime(long x)
    {
        if (x <= 1)
            return false;

        if (x <= 3)
            return true;

        // This is checked so that we can skip
        // middle five numbers in below loop
        if (x % 2 == 0 || x % 3 == 0)
            return false;

        for (long i = 5; i * i <= x; i = i + 6)
        {
            if (x % i == 0 || x % (i + 2) == 0)
                return false;
        }

        return true;
    }

    // We get our d value from this
    // This uses the EEA
    public static long getD(long e, long p1q1)
    {
        long k1 = 1, d1 = 0, k2 = 0, d2 = 1, temp = 1, q = 1, le = e, lp1q1 = p1q1;
    
        // How this will work is using the EEA, but we're shifting
        // k and d by a row each loop
        // Remember, row_i = row_i-2 - (row_i-1 * q_i)
        while(lp1q1 % le != 0)
        {
            // First, we need q for the calculations
            q = (long)(Math.floor(lp1q1 / le));

            // Second, do k
            temp = k1;
            k1 = k2;
            k2 = temp - (k1 * q);

            // Next, do d
            temp = d1;
            d1 = d2;
            d2 = temp - (d1 * q);

            // Finally, n becomes e, and the new e is (n - (q * e))
            temp = (lp1q1 - (q * le));
            lp1q1 = le;
            le = temp;
        }

        // d may or may not be negative, so we need to check
        // If it is, just add p1q1 to it
        if(d2 < 0)
        {
            d2 = d2 + p1q1;
        }

        return d2;
    }

    
    // I changed this to BigInteger, since the loop will become ineffective with larger primes.
    // There were at least 5 iterations where I attempted to use recursion, where I cut the size
    // of the exponent in half. Unfortunately, it proved to be unpredictable :(
    // In the future, I may change ALL the types from long to BigInteger, as it can support
    // even larger values.
    public static long getCorR(long MorC, long eOrd, long n)
    {        
        BigInteger base = BigInteger.valueOf(MorC);
        BigInteger power = BigInteger.valueOf(eOrd);
        BigInteger newN = BigInteger.valueOf(n);
        return (base.modPow(power, newN)).longValue();
    }
}