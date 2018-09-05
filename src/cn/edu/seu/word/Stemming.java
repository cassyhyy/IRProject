package cn.edu.seu.word;

import java.io.*;

public class Stemming {
	private char[] b;
	private int i, i_end, j, k;// b中的元素位置（偏移量） 要抽取词干单词的结束位置
	private static final int INC =50;
	
	public Stemming(){
		b = new char[INC];
		i = 0;
		i_end = 0;
	}
	
	//增加一个字符到要存放待处理的单词的数组。添加完字符时，可以调用stem(void)方法来进行抽取词干的工作。
	public void add(char ch) {
		if( i == b.length){
			char[] new_b = new char[i + INC];
			for (int c = 0; c < i; c++)
				new_b[c] = b[c];
			b = new_b;
		}
		b[i++] = ch;
	}
	
	//增加wLen长度的字符数组到存放待处理的单词的数组b
	public void add(char[] w, int wLen){
		if (i+wLen >= b.length){
			char[] new_b = new char[i + wLen + INC];
	        for (int c = 0; c < i; c++) 
	        	new_b[c] = b[c];
	        b = new_b;
	      }
		for (int c = 0; c < wLen; c++) 
			b[i++] = w[c];
	}
	
	public String toString() { return new String(b,0,i_end); }

	public int getResultLength() { return i_end; }

	public char[] getResultBuffer() { return b; }

	//判断是否为辅音
	private final boolean cons(int i){
		switch (b[i]){
		case 'a': case 'e': case 'i': case 'o': case 'u': return false;
		case 'y': return (i==0) ? true : !cons(i-1);
		default: return true;
		}
	}

   //表示单词b介于0和j之间辅音序列的个度
   private final int m()
   {  int n = 0;
      int i = 0;
      while(true)
      {  if (i > j) return n;
         if (! cons(i)) break; i++;
      }
      i++;
      while(true)
      {  while(true)
         {  if (i > j) return n;
               if (cons(i)) break;
               i++;
         }
         i++;
         n++;
         while(true)
         {  if (i > j) return n;
            if (! cons(i)) break;
            i++;
         }
         i++;
       }
   }

   //表示单词b介于0到i之间是否存在元音
   private final boolean vowelinstem()
   {  int i; for (i = 0; i <= j; i++) if (! cons(i)) return true;
      return false;
   }

   //在j和j-1位置上的两个字符是否是相同的辅音
   private final boolean doublec(int j)
   {  if (j < 1) return false;
      if (b[j] != b[j-1]) return false;
      return cons(j);
   }

   //对于i，i-1，i-2位置上的字符，它们是“辅音-元音-辅音”的形式，并且对于第二个辅音，它不能为w、x、y中的一个。
   //这个函数用来处理以e结尾的短单词。比如说cav(e)，lov(e)，hop(e)，crim(e)。但是像snow，box，tray就辅符合条件。
   private final boolean cvc(int i)
   {  if (i < 2 || !cons(i) || cons(i-1) || !cons(i-2)) return false;
      {  int ch = b[i];
         if (ch == 'w' || ch == 'x' || ch == 'y') return false;
      }
      return true;
   }

   //判断b是否以s结尾
   private final boolean ends(String s)
   {  int l = s.length();
      int o = k-l+1;
      if (o < 0) return false;
      for (int i = 0; i < l; i++) if (b[o+i] != s.charAt(i)) return false;
      j = k-l;
      return true;
   }

   //把b在(j+1)...k位置上的字符设为s，同时，调整k的大小。
   private final void setto(String s)
   {  int l = s.length();
      int o = j+1;
      for (int i = 0; i < l; i++) b[o+i] = s.charAt(i);
      k = j+l;
   }

   //在m()>0的情况下，调用setto(s)
   private final void r(String s) { if (m() > 0) setto(s); }

	   /* step1() gets rid of plurals and -ed or -ing. e.g.

	          caresses  ->  caress
	          ponies    ->  poni
	          ties      ->  ti
	          caress    ->  caress
	          cats      ->  cat

	          feed      ->  feed
	          agreed    ->  agree
	          disabled  ->  disable

	          matting   ->  mat
	          mating    ->  mate
	          meeting   ->  meet
	          milling   ->  mill
	          messing   ->  mess

	          meetings  ->  meet

	   */
   //处理复数、ed结尾和ing结尾
   private final void step1()
   {  if (b[k] == 's')
      {  if (ends("sses")) k -= 2; else
         if (ends("ies")) setto("i"); else
         if (b[k-1] != 's') k--;
      }
      if (ends("eed")) { if (m() > 0) k--; } else
      if ((ends("ed") || ends("ing")) && vowelinstem())
      {  k = j;
         if (ends("at")) setto("ate"); else
         if (ends("bl")) setto("ble"); else
         if (ends("iz")) setto("ize"); else
         if (doublec(k))
         {  k--;
            {  int ch = b[k];
               if (ch == 'l' || ch == 's' || ch == 'z') k++;
            }
         }
         else if (m() == 1 && cvc(k)) setto("e");
     }
   }

	   /* step2() turns terminal y to i when there is another vowel in the stem. */

   private final void step2() { if (ends("y") && vowelinstem()) b[k] = 'i'; }

	   /* step3() maps double suffices to single ones. so -ization ( = -ize plus
	      -ation) maps to -ize etc. note that the string before the suffix must give
	      m() > 0. */

   private final void step3() { if (k == 0) return; /* For Bug 1 */ switch (b[k-1])
   {
       case 'a': if (ends("ational")) { r("ate"); break; }
                 if (ends("tional")) { r("tion"); break; }
                 break;
       case 'c': if (ends("enci")) { r("ence"); break; }
                 if (ends("anci")) { r("ance"); break; }
                 break;
       case 'e': if (ends("izer")) { r("ize"); break; }
                 break;
       case 'l': if (ends("bli")) { r("ble"); break; }
                 if (ends("alli")) { r("al"); break; }
                 if (ends("entli")) { r("ent"); break; }
                 if (ends("eli")) { r("e"); break; }
                 if (ends("ousli")) { r("ous"); break; }
                 break;
       case 'o': if (ends("ization")) { r("ize"); break; }
                 if (ends("ation")) { r("ate"); break; }
                 if (ends("ator")) { r("ate"); break; }
                 break;
       case 's': if (ends("alism")) { r("al"); break; }
                 if (ends("iveness")) { r("ive"); break; }
                 if (ends("fulness")) { r("ful"); break; }
                 if (ends("ousness")) { r("ous"); break; }
                 break;
       case 't': if (ends("aliti")) { r("al"); break; }
                 if (ends("iviti")) { r("ive"); break; }
                 if (ends("biliti")) { r("ble"); break; }
                 break;
       case 'g': if (ends("logi")) { r("log"); break; }
   } }

	   /* step4() deals with -ic-, -full, -ness etc. similar strategy to step3. */

   private final void step4() { switch (b[k])
   {
       case 'e': if (ends("icate")) { r("ic"); break; }
                 if (ends("ative")) { r(""); break; }
                 if (ends("alize")) { r("al"); break; }
                 break;
       case 'i': if (ends("iciti")) { r("ic"); break; }
       			 break;
       case 'l': if (ends("ical")) { r("ic"); break; }
                 if (ends("ful")) { r(""); break; }
                 break;
       case 's': if (ends("ness")) { r(""); break; }
                 break;
	   } 
   }

	   /* step5() takes off -ant, -ence etc., in context <c>vcvc<v>. */

   private final void step5()
   {   if (k == 0) return; /* for Bug 1 */ switch (b[k-1])
       {  case 'a': if (ends("al")) break; return;
          case 'c': if (ends("ance")) break;
                    if (ends("ence")) break; return;
          case 'e': if (ends("er")) break; return;
          case 'i': if (ends("ic")) break; return;
          case 'l': if (ends("able")) break;
                    if (ends("ible")) break; return;
          case 'n': if (ends("ant")) break;
                    if (ends("ement")) break;
                    if (ends("ment")) break;
                    /* element etc. not stripped before the m */
                    if (ends("ent")) break; return;
          case 'o': if (ends("ion") && j >= 0 && (b[j] == 's' || b[j] == 't')) break;
                                    /* j >= 0 fixes Bug 2 */
                    if (ends("ou")) break; return;
                    /* takes care of -ous */
          case 's': if (ends("ism")) break; return;
          case 't': if (ends("ate")) break;
                    if (ends("iti")) break; return;
          case 'u': if (ends("ous")) break; return;
          case 'v': if (ends("ive")) break; return;
          case 'z': if (ends("ize")) break; return;
          default: return;
       }
       if (m() > 1) k = j;
   }

	   /* step6() removes a final -e if m() > 1. */

	   private final void step6()
	   {  j = k;
	      if (b[k] == 'e')
	      {  int a = m();
	         if (a > 1 || a == 1 && !cvc(k-1)) k--;
	      }
	      if (b[k] == 'l' && doublec(k) && m() > 1) k--;
	   }

	   /** Stem the word placed into the Stemmer buffer through calls to add().
	    * Returns true if the stemming process resulted in a word different
	    * from the input.  You can retrieve the result with
	    * getResultLength()/getResultBuffer() or toString().
	    */
	   public void stem()
	   {  k = i - 1;
	      if (k > 1) { step1(); step2(); step3(); step4(); step5(); step6(); }
	      i_end = k+1; i = 0;
	   }

	   /** Test program for demonstrating the Stemmer.  It reads text from a
	    * a list of files, stems each word, and writes the result to standard
	    * output. Note that the word stemmed is expected to be in lower case:
	    * forcing lower case must be done outside the Stemmer class.
	    * Usage: Stemmer file-name file-name ... 
	    */
	   
	   public void stem(String inFileName, String outFileName){
		   char[] w = new char[501];
		   Stemming s = new Stemming();
		   try     
		      {     
		         FileInputStream in = new FileInputStream(inFileName);     
		         FileOutputStream out = new FileOutputStream(outFileName);     		              
		         try     
		         { while(true)     		     
		           {  int ch = in.read();     		                    
		              if (ch > 0xa0)      
		               {     
		                   out.write(ch);     
		                   continue;     		                     
		               }     		                   
		              else if (Character.isLetter((char) ch))     
		              {     
		                 int j = 0;     
		                 while(true)     
		                 {  ch = Character.toLowerCase((char) ch);     
		                    w[j] = (char) ch;     
		                    if (j < 500) j++;     
		                    ch = in.read();     
		                    if (!Character.isLetter((char) ch) || ch > 0xa0)  // Read to the end of one word     
		                    {     
		                       /* to test add(char ch) */     
		                       for (int c = 0; c < j; c++) s.add(w[c]);     
		     
		                       /* or, to test add(char[] w, int j) */     
		                       /* s.add(w, j); */     		     
		                       s.stem();     
		                       {  String u;     
		     
		                          /* and now, to test toString() : */     
		                          u = s.toString();     
		     
		                          /* to test getResultBuffer(), getResultLength() : */     
		                          /* u = new String(s.getResultBuffer(), 0, s.getResultLength()); */     
		                          int len=u.length();     
		                          for (int k=0;k<len;k++)     
		                          out.write((char)(u.charAt(k)));     		     
		                 //         out.write(u);     
		                       }     
		                       break;     
		                    }     
		                 }     
		              }     
		                   
		              if (ch < 0) break;     
		              out.write((char)ch);     
		           }     
		         }     
		         catch (IOException e)     
		         {  System.out.println("error reading " + inFileName + "or write "+ outFileName + "error!");     
		                    }     
		      }     
		      catch (FileNotFoundException e)     
		      {  System.out.println("file " + inFileName + " not found" + "or" + "file" +outFileName + "open error");     
		      }  
	   }
	   
	   
	   public String stem(String str){
		   for(int i=0;i<str.length();i++)
			   add(str.charAt(i));
		   
		   stem();
		   return toString();
	   }
	      
	   
	   public static void main(String []argv)     
	   { 
		   String spath;     
		   String dpath,dpath1;     
		   String sfile = "E:\\大学\\信息检索\\歌词";     
		   String dfile = ".\\歌词";     
		   File sfolder = new File(sfile);     
		   File[ ] entries = sfolder.listFiles( );     
		   Stemming s = new Stemming();     
		   for(int i=0; i<entries.length; i++) {     
			   spath = entries[i].getPath();     
			   dpath1 =dfile+"\\"+entries[i].getName();     
			   File subfolder = new File(spath);     
			   File[] subentries = subfolder.listFiles();     
			   for(int j=0; j<subentries.length;j++)     
			   {     
				   spath = subentries[j].getPath();     
				   dpath = dpath1+"\\"+subentries[j].getName();     
				   try{     
					   FileWriter fw = new FileWriter(dpath);     
					   fw.close();     
				   }catch (IOException e)     
				   {  System.out.println("error!");     
				   }     
				   s.stem(spath,dpath);     
			   }     

		   } 
	   }     
	   
}