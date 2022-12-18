import java.util.*;
import java.io.*;

public class RSA
{

public RSA()
{
}

//Takes an array of characters and encrypts it
public void encrypt(char[] input) throws Exception 
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

    //Now we can take the ArrayList input and do M^e = C, and put each character into the file

    char[] cinput = input;
    FileWriter fw = new FileWriter("Encrypted");
	PrintWriter pw = new PrintWriter(fw);
    long M = 0;

    //Let's first put in the decryption key
    pw.println(d);
    pw.println(n);

    for(int i = 0; i < cinput.length; i++)
    {
        M = (int)cinput[i];
        pw.println(getC(M, e, n));
    }
    
    //In an actual application of RSA, what would happen is that the
    //public and private keys are made by A, the public key is given to B
    //so that they can encrypt their message, and then B sends that message
    //to A, since ONLY A has the private key. We send the public key, as it
    //is ONLY used for encryption, NOT decryption, so Eavesdroppers cannot
    //parse out the message

    //Public Key (e,n)
    //Private Key (d,n)

    System.out.println("Done!");

    pw.close();
    fw.close();
    in.close();
}


//Takes a file and decrypts it
public void decrypt() throws IOException
    {
        //Scan the encrypted file to get the keys and hidden message
        Scanner in = new Scanner(new File("Encrypted"));

        long d = Integer.parseInt(in.next());
        long n = Integer.parseInt(in.next());

        //Now the rest of the file contains the encrypted message

        System.out.println(d);
        System.out.println(n);

        //Now, solve for R, which is R^d % n
        //Again, same reason why we can't just do Math.pow,
        //the number just gets too big and so we'de lose data

        //Here, we will loop through the file, and put the output into a file
        FileWriter fw = new FileWriter("Decrypted");
	    PrintWriter pw = new PrintWriter(fw);
        long C = 0;
        String OM = "";

        while(in.hasNext())
        {
            C = in.nextInt();
            long R = getR(C, d, n);
            System.out.println(R);
            OM = OM + (char)R;
        }

        pw.println(OM);

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