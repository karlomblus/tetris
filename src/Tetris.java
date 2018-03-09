import java.util.Arrays;
import java.lang.String;

public class Tetris
{
    private String seis;
    public Tetris(String seis)
    {
        this.seis = seis;
    }

    String arvutaUusSeis(String seis, String sündmus)
    {
        String[] read = seis.split("\\n");
        if (sündmus.toLowerCase().equals("oota"))
        {
            oota(read);
        }
        else if (sündmus.toLowerCase().equals("vasak"))
        {
            vasak(read);
        }
        else if (sündmus.toLowerCase().equals("exit"))
        {

        }
        else if (sündmus.toLowerCase().equals("parem"))
        {
            parem(read);
        }
        else if (sündmus.equals(" ") || sündmus.equals("tühik"))
        {
            tühik(read);
        }
        prindiRead(read);
        String uusSeis = "";
        for (int i = 0; i < read.length; i++)
        {
            uusSeis += read[i] + "\n";
        }
        return uusSeis;
    }
    boolean oota(String [] read)
    {
        boolean koikKlotsidLiigutatavad = true;
        String[] ajutine = Arrays.copyOf(read, read.length);
        for (int i = read.length - 1; i >= 0; i--) //loopime read l2bi alustades k8ige alumisest
        {
            for (int j = 0; j < read[i].length(); j++)
            {
                if (read[i].charAt(j) == '#')
                {

                    if (i+1 <= (ajutine.length - 1) && ajutine[i+1].charAt(j) != 'X' && ajutine[i+1].charAt(j) != '#')//Kui eksisteerib rida olemasoleva all ja selle all smbol pole X
                    {
                        System.out.println("SEES");
                        StringBuilder sb = new StringBuilder(ajutine[i+1]);
                        sb.setCharAt(j, '#');
                        ajutine[i+1] = sb.toString();
                        sb = new StringBuilder(ajutine[i]);
                        sb.setCharAt(j, '-');
                        ajutine[i] = sb.toString();
                    }
                    else //Kui kõige alumine klotsiosa ei saa kukkuda, siis ei saa ükski ülemine ka kukkuda
                    {
                        koikKlotsidLiigutatavad = false;
                    }
                }
            }
        }
        if (koikKlotsidLiigutatavad == true)
        {
            for (int i = 0; i < ajutine.length; i++)
            {
                for (int j = 0; j < ajutine[i].length(); j++)
                {
                    read[i] = ajutine[i];
                }
            }
        }
        return koikKlotsidLiigutatavad;
    }
    void vasak(String [] read)
    {
        boolean koikKlotsidLiigutatavad = true;
        String[] ajutine = Arrays.copyOf(read, read.length);
        for (int i = read.length - 1; i >= 0 ; i--)
        {
            for (int j = 0; j < read[i].length(); j++)
            {
                if (read[i].charAt(j) == '#')
                {
                    if ((j - 1) >= 0 && ajutine[i].charAt(j-1) != 'X' && ajutine[i].charAt(j-1) != '#')
                    {
                        StringBuilder sb = new StringBuilder(ajutine[i]);
                        sb.setCharAt(j-1, '#');
                        ajutine[i] = sb.toString();
                        sb = new StringBuilder(ajutine[i]);
                        sb.setCharAt(j, '-');
                        ajutine[i] = sb.toString();
                    }
                    else
                    {
                        koikKlotsidLiigutatavad = false;
                    }
                }
            }
        }
        if (koikKlotsidLiigutatavad == true)
        {
            for (int i = 0; i < ajutine.length; i++)
            {
                for (int j = 0; j < ajutine[i].length(); j++)
                {
                    read[i] = ajutine[i];
                }
            }
        }
    }
    void parem(String [] read)
    {
        String[] ajutine = Arrays.copyOf(read, read.length);
        boolean koikKlotsidLiigutatavad = true;
        for (int i = read.length - 1; i >= 0; i--)
        {
            for (int j = read[i].length() - 1; j >= 0; j--)
            {
                if (read[i].charAt(j) == '#')
                {
                    if ((j + 1) <= (ajutine[i].length() - 1) && ajutine[i].charAt(j+1) != 'X' && ajutine[i].charAt(j+1) != '#')
                    {
                        StringBuilder sb = new StringBuilder(ajutine[i]);
                        sb.setCharAt(j+1, '#');
                        ajutine[i] = sb.toString();
                        sb = new StringBuilder(ajutine[i]);
                        sb.setCharAt(j, '-');
                        ajutine[i] = sb.toString();
                    }
                    else
                    {
                        koikKlotsidLiigutatavad = false;
                    }
                }
            }

        }
        if (koikKlotsidLiigutatavad == true)
        {
            for (int i = 0; i < ajutine.length; i++)
            {
                for (int j = 0; j < ajutine[i].length(); j++)
                {
                    read[i] = ajutine[i];
                }
            }
        }
    }
    void tühik(String[] read)
    {
        while (oota(read) == true);

    }
    void prindiRead(String[] seis)
    {
        for(String read : seis)
        {
            System.out.println(read);
        }
    }
    String tetrisArrayToString(String [] seis)
    {
        String uusSeis = "";
        for (int i = 0; i < seis.length; i++)
        {
            uusSeis += seis[i] + "\n";
        }
        return uusSeis;
    }

    public String getSeis()
    {
        return seis;
    }

    public void setSeis(String seis)
    {
        this.seis = seis;
    }
}
