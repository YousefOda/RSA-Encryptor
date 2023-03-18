import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import javax.swing.*;

// Frame for user input and interaction
public class Frame extends JFrame implements ActionListener
{
    // Buttons
    JButton encrypt;
    JButton decrypt;

    // User typeable text area
    JTextArea type;

    // Frame
    JFrame frame;

    RSA enc = new RSA();

    Frame() throws IOException
    {
        // Setup for the frame
        encrypt = new JButton("Encrypt");
        encrypt.setBounds(10, 250, 150, 50);
        encrypt.addActionListener(this);

        decrypt = new JButton("Decrypt");
        decrypt.setBounds(170, 250, 150, 50);
        decrypt.addActionListener(this);

        type = new JTextArea();
        type.setBounds(10, 10, 300, 200);

        frame = new JFrame();
		frame.setTitle("RSA Encrytor/Decryptor");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(null);
		frame.setResizable(false);
		frame.setSize(340, 350);
		frame.setVisible(true);
        frame.add(encrypt);
        frame.add(decrypt);
        frame.add(type);
    }

    // What happens when a button is pressed
    public void actionPerformed(ActionEvent e)
    {
         
        if(e.getSource() == encrypt)
        {
            String input = type.getText();
            char[] cinput = input.toCharArray();

            try 
            {
                enc.encrypt(cinput);
            } 
            catch (Exception exception) 
            {
                exception.printStackTrace();
            }
        }

        else
        {
            try 
            {
                enc.decrypt(type.getText());
            } 

            catch (IOException exception) 
            {
                exception.printStackTrace();
            }
        }
    }
}
