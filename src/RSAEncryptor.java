import java.util.*;
import java.io.*;

public class RSAEncryptor 
{
public static void main(String[] args) throws Exception 
{
    ArrayList<Integer> lop = listOfPrimes();
    Scanner in = new Scanner(System.in);

    //Choose LARGE, DISTINCT primes p,q
    Random rand = new Random();
    long p = lop.get(rand.nextInt(0, lop.size()));
    long q = 0;

    //This loop is to check if q is distinct from p
    while(true)
    {
        q = lop.get(rand.nextInt(0, lop.size()));

        if(p != q)
        {
            break;
        }
    }

    //n = pq and (p-1)(q-1)
    long n = p * q;
    long p1q1 = (p-1)*(q-1);

    System.out.println("p = " + p);
    System.out.println("q = " + q);
    System.out.println("n = " + n);
    System.out.println("(p-1)(q-1) = " + p1q1);

    //Choose e,d such that ed = 1 (mod (p-1)(q-1)) and
    //1 < e,d < (p-1)(q-1)
    long e = getE(p1q1);
    System.out.println("e = " + e);

    //Now, we need d by solving an LDE/LCT
    //To do this, we use EEA
    long d = getD(e, p1q1);
    System.out.println("d = " + d);
    System.out.println("ed (mod " + p1q1 + ") = " + ((e*d) % p1q1));

    //Now we get M
    System.out.print("Please enter a numeric message between 0 (inclusive) and " + n + " (exclusive): ");
    long M = in.nextInt();
    System.out.println("Message: " + M);

    //Now we must compute M^e = C (mod n), 0 <= C < n
    //The reason why we don't do Math.pow is that the numbers are just so big,
    //we would actually lose data just by doing that (bit overflow), so we made a loop
    long C = getC(M, e, n);
    System.out.println("C = " + C);

    //NOW, C is our encrypted message!
    //Let's go ahead and put C into a file along with the Private key for future decryption!
    
    //In an actual application of RSA, what would happen is that the
    //public and private keys are made by A, the public key is given to B
    //so that they can encrypt their message, and then B sends that message
    //to A, since ONLY A has the private key. We send the public key, as it
    //is ONLY used for encryption, NOT decryption, so Eavesdroppers cannot
    //parse out the message

    FileWriter fw = new FileWriter("Encrypted");
	PrintWriter pw = new PrintWriter(fw);

    //Public Key (e,n)
    //Private Key (d,n)
    
    pw.println(d);
    pw.println(n);
    pw.println(C);
    pw.println("Actual Message M for comparison: " + M);

    pw.close();
    fw.close();
    in.close();
}

//This creates a "table" of primes that we can randomly choose
public static ArrayList<Integer> listOfPrimes()
{
    ArrayList<Integer> lop = new ArrayList<Integer>();

    for(int i = 1000; i < 2000; i++)
    {
        if(isPrime(i))
        {
            lop.add(i);
        }
    }

    return lop;
}

//This helps us determine if a number is prime
//I believe this is O(nlog(n))
public static boolean isPrime(int x)
{
    if (x <= 1)
        return false;

    if (x <= 3)
        return true;

        // This is checked so that we can skip
        // middle five numbers in below loop
        if (x % 2 == 0 || x % 3 == 0)
            return false;

        for (int i = 5; i * i <= x; i = i + 6)
        {
            if (x % i == 0 || x % (i + 2) == 0)
                return false;
        }

        return true;
}

//We get our e value from this
public static long getE(long p1q1)
{
    long e = 0;

    for(e = 3; e < p1q1; e++)
    {
        if((p1q1 % e) == 1) 
        {
            break;
        }
    }

    return e;
}

//We get our d value from this
//This uses the EEA
public static long getD(long e, long p1q1)
{
    long k1 = 1, d1 = 0;
    long k2 = 0, d2 = 1;
    long temp = 1;
    long q = 1;
    long le = e;
    long lp1q1 = p1q1;
    
    //How this will work is using the EEA, but we're shifting
    //k and d by a row each loop
    //Remember, row3 = row1 - (row2 * q3)
    for(;(lp1q1 % le != 0);)
    {
        //First, we need q for the calculations
        q = (long)(Math.floor(lp1q1 / le));

        //Second, do k
        temp = k1;
        k1 = k2;
        k2 = temp - (k1 * q);

        //Next, do d
        temp = d1;
        d1 = d2;
        d2 = temp - (d1 * q);

        //Finally, n becomes e, and the new e is (n - (q * e))
        temp = (lp1q1 - (q * le));
        lp1q1 = le;
        le = temp;
    }

    //d may or may not be negative, so we need to check
    //If it is, just add p1q1 to it
    if(d2 < 0)
    {
        d2 = d2 + p1q1;
    }

    return d2;
}

//This is the loop to give us our C value
public static long getC(long M, long e, long n)
{
    long base = M, power = e, newN = n;
    long C = 1;

    //Running loop while the power > 0
    while (power != 0) 
    {
        C = ((C * base) % newN);
        //Power will get reduced after each multiplication
        power--;
    }

    return C;
}
}