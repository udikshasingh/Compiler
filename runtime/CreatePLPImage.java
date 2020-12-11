package cop5556fa20.runtime;


import java.awt.Dimension;
import java.awt.image.BufferedImage;
import static cop5556fa20.Scanner.Kind;
import static cop5556fa20.Scanner.Kind.LARROW;
import static cop5556fa20.Scanner.Kind.ASSIGN;

 public class CreatePLPImage {

    public static String assignSig = "(Lcop5556fa20/runtime/PLPImage;Lcop5556fa20/runtime/PLPImage;II)Lcop5556fa20/runtime/PLPImage;";
    public static String copySig = "(Lcop5556fa20/runtime/PLPImage;Ljava/lang/Object;)Lcop5556fa20/runtime/PLPImage;";
    public static String createSig = "(Lcop5556fa20/Scanner$Kind;Ljava/lang/Integer;Ljava/lang/Integer;Ljava/lang/Object;II)Lcop5556fa20/runtime/PLPImage;";

    public static PLPImage assign(PLPImage src, PLPImage dest, int posInLine, int line) throws Exception {
    	
        if (dest == null || dest.image == null) 
            throw new PLPImage.PLPImageException(line, posInLine, "Source image cannot be null");
        
        if (src.declaredSize != null) {
        	int widththrows = dest.getWidthThrows(line, posInLine);
        	int heightthrows = dest.getHeightThrows(line, posInLine);
            if (src.declaredSize.width != widththrows || src.declaredSize.height != heightthrows) 
                throw new PLPImage.PLPImageException(line, posInLine, "Mismath in the size of the image");
        }
        
        src.image = dest.image;
        return src;
    }
   
    public static PLPImage copy(PLPImage img, Object obj) throws Exception {
        BufferedImage bi = null;
        
        if (obj instanceof PLPImage) {
        	BufferedImage bufferedImage = ((PLPImage)obj).image;
        	bi = BufferedImageUtils.copyBufferedImage(bufferedImage);
        }
        
        else if (obj instanceof String) {
        	String url = (String)obj;
        	bi = BufferedImageUtils.fetchBufferedImage(url);
        }
       
        if (img.declaredSize != null) {
        	int newWidth = img.declaredSize.width;
        	int newHeight = img.declaredSize.height;
            bi = BufferedImageUtils.resizeBufferedImage(bi, newWidth, newHeight);
        }
        
        img.image = bi;
        
        return img;
    }
    
    public static PLPImage create(Kind kind, Integer w, Integer h, Object obj, int posInLine, int line) throws Exception {
       
    	Dimension dim = null;
        BufferedImage bi = null;

        if (w != null) 
            dim = new Dimension(w, h);
        
        if (kind == ASSIGN) {
            if (dim != null) {
            	
                int width = ((PLPImage)obj).getWidthThrows(line, posInLine);
                int height = ((PLPImage)obj).getHeightThrows(line, posInLine);
                if (w != width || h != height) 
                    throw new PLPImage.PLPImageException(line, posInLine, "Mismatch in the size of image");
            
            }
            bi = ((PLPImage)obj).image;
        }
                
        else if (kind == LARROW) {
        	
            if (obj instanceof String) {
            	String url = (String) obj;
            	bi = BufferedImageUtils.fetchBufferedImage(url);
            }
            
            else if (obj instanceof PLPImage) {
            	BufferedImage img = ((PLPImage) obj).image;
                bi = BufferedImageUtils.copyBufferedImage(img);
            }
            
            if (dim != null) {
                bi = BufferedImageUtils.resizeBufferedImage(bi, w, h);
            }
            
        }

        PLPImage plpimage = new PLPImage(bi, dim);
        return plpimage;
    }
}
