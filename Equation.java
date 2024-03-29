import java.math.BigDecimal;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.math.RoundingMode;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.awt.Shape;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class Equation
{
   private int maxPoints = 1000;
   private String userEquation;
   //curve
   private ArrayList<BigDecimal> yCoordArray = new ArrayList<>();
   private ArrayList<String> degrees = new ArrayList<>();
   //line and curve
   private ArrayList<BigDecimal> coefficients = new ArrayList<>();
   private ArrayList<String> terms = new ArrayList<>();
   private ArrayList<String> sides = new ArrayList<>();
   //line
   private BigDecimal m = BigDecimal.valueOf(1);
   private BigDecimal b = BigDecimal.valueOf(0);
   //Point
   private BigDecimal userPointX;
   private BigDecimal userPointY;
   private BigDecimal pointSize = BigDecimal.valueOf(5);
   //importing stuff
   private BigDecimal xScreenSize;
   private BigDecimal yScreenSize;
   private BigDecimal zoomFactor;
   private BigDecimal panX;
   private BigDecimal panY;
   private BigDecimal tickInterval;
   private BigDecimal zoomDynamic;
   public static final BigDecimal e = BigDecimal.valueOf(2.71828182845904523536028747135266249775724709369995957496696762772407663035354759457);
   public static final BigDecimal pi = BigDecimal.valueOf(3.1415926535897932384626433832795028841971693993751058209749445923078164062862089986);
   private static final BigDecimal DEGREES_TO_RADIANS = BigDecimal.valueOf(0.017453292519943295);
   private static final BigDecimal RADIANS_TO_DEGREES = BigDecimal.valueOf(57.29577951308232);
   public Equation(String initEquation)
   {
      userEquation = initEquation.replaceAll(" ", "");//REMOVES SPACES
      if(userEquation.equals(""))//THROWS OUT EMPTY EQUATIONS
      {
         System.out.println("Empty equation!");
      }
      else if(userEquation.contains(","))//IS A POINT
      {
         String[] temp = userEquation.split(",");
         userPointX = BigDecimal.valueOf(Double.valueOf(temp[0].replaceAll("[^\\d.]", "")));//gets rid of any non number/decimal values
         userPointY = BigDecimal.valueOf(Double.valueOf(temp[1].replaceAll("[^\\d.]", "")));
      }
      else if(userEquation.contains("y"))
      {
         sides.add(userEquation.split("=")[0]);
         sides.add(userEquation.split("=")[1]);
         splitAndProcessTerms(sides.get(1));
         System.out.println("terms: " + terms);
      }
      else//only equation in form of x^2 instead of y = x^2
      {
         sides.add(userEquation);
         System.out.println("sides0: " + sides.get(0));
         splitAndProcessTerms(sides.get(0));
         System.out.println("terms2: " + terms);
      }
   }
   public void drawUserEquation(Graphics2D graphics, BigDecimal initXScreenSize, BigDecimal initYScreenSize, BigDecimal initZoomFactor, BigDecimal initPanX, BigDecimal initPanY, BigDecimal initTickInterval, BigDecimal initZoomDynamic)
   {
      xScreenSize = initXScreenSize;
      yScreenSize = initYScreenSize;
      zoomFactor = initZoomFactor;
      panX = initPanX;
      panY = initPanY;
      tickInterval = initTickInterval;
      zoomDynamic = initZoomDynamic;
      if(userEquation.contains(","))
      {
         drawUserPoint(graphics);
      }
      else
      {
         drawUserCurve(graphics);
      }
   }
   public void drawUserPoint(Graphics2D graphics)
   {
      BigDecimal newX = coordsToScreenX(userPointX);
      BigDecimal newY = coordsToScreenY(userPointY);
      Shape Circle = new Ellipse2D.Double(newX.doubleValue(), newY.doubleValue(), pointSize.doubleValue(), pointSize.doubleValue());
      graphics.fill(Circle);
   }
   public void drawUserCurve(Graphics2D graphics)
   {
      //iterates through screen
      for(BigDecimal i = BigDecimal.valueOf(0); i.compareTo(BigDecimal.valueOf(2).multiply(xScreenSize)) < 0; i = i.add(BigDecimal.valueOf(2).multiply(xScreenSize).divide(BigDecimal.valueOf(maxPoints))))
      {
         boolean isComplex = false;
         //total for specific x value
         BigDecimal total = BigDecimal.valueOf(0);
         //evaluates each term
         for(int k = 0; k < terms.size(); k++)
         {
            //applies exponent and converts to coords
            double exponentResult;
            if(isTrigFunction(terms.get(k)))
            {
               BigDecimal multiplyCoef = getCoefficient(terms.get(k).substring(terms.get(k).indexOf("(")+1,terms.get(k).indexOf("x")+1)).multiply(screenXToCoords(i));
               //System.out.println("mult: " + terms.get(k).substring(terms.get(k).indexOf("\\(")+1,terms.get(k).indexOf("x")+1));
               exponentResult = Double.valueOf(evaluateTrigFunction(terms.get(k).substring(0,terms.get(k).indexOf("(")+1) + String.valueOf(multiplyCoef) + ")"));
               //exponentResult = Double.valueOf(String.valueOf(evaluateTrigFunction(terms.get(k).replaceAll("x", String.valueOf(screenXToCoords(i))))));
            }
            else
            {
               if(!degrees.get(k).contains("x"))
               {
                  exponentResult = Math.pow(screenXToCoords(i).doubleValue(), Double.valueOf(degrees.get(k)));
               }
               else
               {
                  String corrected = degrees.get(k).replaceAll("x", String.valueOf(screenXToCoords(i)));
                  exponentResult = Math.pow(screenXToCoords(i).doubleValue(), Double.valueOf(corrected));
               }
            }
            if(!Double.isNaN(exponentResult) && !Double.isInfinite(exponentResult))
            {
               if(!sides.get(0).contains("y"))
               {
                  //System.out.println("coefficients k: " + coefficients.get(k) + " exponentResult: " + exponentResult + " coords: " + screenXToCoords(i));
                  total = total.add(coefficients.get(k).multiply(BigDecimal.valueOf(exponentResult)));
               }
               else
               {
                  total = total.add(coefficients.get(k).multiply(BigDecimal.valueOf(exponentResult)).divide(getCoefficientY(sides.get(0))));
               }
            }
            else
            {
               isComplex = true;
            }
         }
         if(!isComplex)
         {
            yCoordArray.add(coordsToScreenY(total));
            if(yCoordArray.size() >= 2)
            {
               BigDecimal x1 = i.subtract(BigDecimal.valueOf(2).multiply(xScreenSize).divide(BigDecimal.valueOf(maxPoints)));//i-(2*xScreenSize/maxPoints)
               BigDecimal y1 = yCoordArray.get(yCoordArray.size() - 2);
               BigDecimal x2 = i;
               BigDecimal y2 = yCoordArray.get(yCoordArray.size() - 1);
               graphics.draw(new Line2D.Double(x1.doubleValue(), y1.doubleValue(), x2.doubleValue(), y2.doubleValue()));
            }
         }
      }
   }
   public void splitAndProcessTerms(String polynomial)
   {
      String[] termArray = polynomial.split("(?<!\\^|\\()(?=[+-])");//splits when + or - not directly after ^ or not directly after (
      for(String i: termArray)
      {
         i = replaceSpecialConstants(i);
         if(isTrigFunction(i))
         {
            i = evaluateTrigFunction(i);
         }
         if(i.contains("^") && !i.contains("x"))
         {//raises constant to constant
            i = String.valueOf(Math.pow(Double.valueOf(i.split("\\^")[0]), Double.valueOf(i.split("\\^")[1])));
         }
         if(i.contains("*"))
         {
            i = String.valueOf(BigDecimal.valueOf(Double.valueOf(i.split("\\*")[0])).multiply(BigDecimal.valueOf(Double.valueOf(i.split("\\*")[1]))));
         }
         if(!i.equals(""))
         {
            terms.add(i);
            if(i.contains("y"))
            {
               coefficients.add(getCoefficientY(i));
               degrees.add(String.valueOf(getDegreeY(i)));
            }
            else
            {
               coefficients.add(getCoefficient(i));
               if(getDegree(i).contains("x"))
               {
                  
               }
               degrees.add(getDegree(i));
            }
         }
      }
   }
   public BigDecimal getCoefficient(String term)
   {
      term = term.replaceAll("\\^", "");
      if(term.equals(""))
      {
         return BigDecimal.valueOf(1);
      }
      else if(term.equals("-"))
      {
         return BigDecimal.valueOf(-1);
      }
      else if(isTrigFunction(term))
      {
         if(term.indexOf("cos") != -1)
         {
            return getCoefficient(term.substring(0,term.indexOf("cos")));
         }
         else if(term.indexOf("sin") != -1)
         {
            return getCoefficient(term.substring(0,term.indexOf("sin")));
         }
         else if(term.indexOf("tan") != -1)
         {
            return getCoefficient(term.substring(0,term.indexOf("tan")));
         }
         else if(term.indexOf("sec") != -1)
         {
            return getCoefficient(term.substring(0,term.indexOf("sec")));
         }
         else if(term.indexOf("csc") != -1)
         {
            return getCoefficient(term.substring(0,term.indexOf("csc")));
         }
         else//cot
         {
            return getCoefficient(term.substring(0,term.indexOf("cot")));
         }
      }
      else if(!term.contains("x"))//constant
      {
         return BigDecimal.valueOf(Double.valueOf(term));
      }
      else if(term.contains("-x"))
      {
         return BigDecimal.valueOf(-1);
      }
      else if(term.contains("x"))
      {
         String temp = term.substring(term.indexOf("(")+1, term.indexOf("x"));
         return getCoefficient(temp);
      }
      else if(term.charAt(0) == '+' || term.charAt(0) == 'x')
      {
         return BigDecimal.valueOf(1);
      }
      else
      {
         String temp = term.substring(0,term.indexOf("x"));
         return BigDecimal.valueOf(Double.valueOf(term.substring(0,term.indexOf("x"))));
      }
   }
   public BigDecimal getCoefficientY(String term)
   {
      if(!term.contains("y"))//constant
      {
         return BigDecimal.valueOf(Double.valueOf(term));
      }
      else if(term.contains("-y"))
      {
         return BigDecimal.valueOf(-1);
      }
      else if(term.charAt(0) == '+' || term.charAt(0) == 'y')
      {
         return BigDecimal.valueOf(1);
      }
      else
      {
         return BigDecimal.valueOf(Double.valueOf(term.substring(0,term.indexOf("y"))));
      }
   }
   public String getDegree(String term)
   {
      if(!term.contains("^") && term.contains("x"))
      {
         return "1";
      }
      if(!term.contains("^") && !term.contains("x"))
      {
         return "0";
      }
      return term.substring(term.indexOf("^")+1);
   }
   public BigDecimal getDegreeY(String term)
   {
      if(!term.contains("^") && term.contains("y"))
      {
         return BigDecimal.valueOf(1);
      }
      if(!term.contains("^") && !term.contains("y"))
      {
         return BigDecimal.valueOf(0);
      }
      return BigDecimal.valueOf(Double.valueOf(term.substring(term.indexOf("^")+1)));
   }
   public boolean isTrigFunction(String function)
   {
      if(function.matches(".*(cos\\(|sin\\(|tan\\(|sec\\(|csc\\(|cot\\().*"))
      {
         return true;
      }
      return false;
   }
   public String evaluateTrigFunction(String function)
   {
      if(function.contains("x"))
      {
         return function;
      }
      function = function.replaceAll("\\)","");
      if(function.contains("cos("))
      {
         /*if(!function.substring(function.indexOf("cos(")+4,function.indexOf("cos(")+5).matches(".*(-|x).*"))
         {
            function = function.substring(function.indexOf("cos(")+5,function.indexOf("
            function = function.substring(0, function.indexOf("cos(")+5) + "*" + function.substring(function.indexOf("cos(")+5);
            System.out.println("aosidj: " + function);
         }*/
         return String.valueOf(Math.cos(2*Double.valueOf(function.substring(function.indexOf("cos(")+4))));
      }
      else if(function.contains("sin("))
      {
         return String.valueOf(Math.sin(2*Double.valueOf(function.substring(function.indexOf("sin(")+4))));
      }
      else if(function.contains("tan("))
      {
         return String.valueOf(Math.tan(2*Double.valueOf(function.substring(function.indexOf("tan(")+4))));
      }
      else if(function.contains("sec("))
      {
         return String.valueOf(1/Math.cos(2*Double.valueOf(function.substring(function.indexOf("sec(")+4))));
      }
      else if(function.contains("csc("))
      {
         return String.valueOf(1/Math.sin(2*Double.valueOf(function.substring(function.indexOf("csc(")+4))));
      }
      else//cot
      {
         return String.valueOf(1/Math.tan(2*Double.valueOf(function.substring(function.indexOf("cot(")+4))));
      }
   }
   public String replaceSpecialConstants(String function)
   {
      if(function.contains("e"))
      {
         if(function.indexOf("e") == 0 || (function.length()-function.indexOf("e")>=2 && !function.substring(function.indexOf("e")-1, function.indexOf("e")+2).equals("sec"))) 
         {
            function = function.replaceAll("e", String.valueOf(e));
         }
      }
      if(function.contains("pi"))
      {
         function = function.replaceAll("pi", String.valueOf(pi));
      }
      return function;
   }
   public BigDecimal coordsToScreenX(BigDecimal initX)
   {//y = x*d*b + c + a inverse: y = (x - c - a)/(d*b)
      //adjusting based on zoom
      initX = initX.multiply(zoomDynamic);//f(initX) = initX*zoomDynamic // y = x*d
      //adjusting based on pan
      initX = initX.add(panX.divide(tickInterval, 32, RoundingMode.HALF_UP));//f(initX) = initX + (panX/tickInterval) // y = x + (c/b)
      //adjusting based on screen size
      initX = initX.multiply(tickInterval).add(xScreenSize);//f(initX) = initX*tickInterval + xScreenSize // y = x*b + a
      return initX;
   }
   public BigDecimal coordsToScreenY(BigDecimal initY)
   {//y = a - x*d*b + c inverse: y = (a - x + c)/(d*b)
      //adjusting based on zoom
      initY = initY.multiply(zoomDynamic);//f(initY) = initY*zoomDynamic // y = x*d
      //adjusting based on pan
      initY = initY.subtract(panY.divide(tickInterval, 32, RoundingMode.HALF_UP));//f(initY) = initY - (panY/tickInterval) // y = x - (c/b)
      //adjusting based on screen size
      initY = yScreenSize.subtract(initY.multiply(tickInterval));//f(initY) = yScreenSize - (initY*tickInterval) //y = a - x*b
      return initY;
   }//a = ScreenSize, b = tickInterval, c = pan, d = zoomDynamic*/
   public BigDecimal screenXToCoords(BigDecimal initX)
   {//y = (x - a - c)/(d*b)
      //adjusting based on screen size
      initX = (initX.subtract(xScreenSize)).divide(tickInterval, 32, RoundingMode.HALF_UP);//f(initX) = (initX - xScreenSize)/tickInterval // y = (x-a)/b
      //adjusting based on pan
      initX = initX.subtract(panX.divide(tickInterval, 32, RoundingMode.HALF_UP));//f(initX) = initX - (panX/tickInterval) // y = x - (c/b)
      //adjusting based on zoom
      initX = initX.divide(zoomDynamic, 32, RoundingMode.HALF_UP);//f(initX) = initX/zoomDynamic // y = x/d
      return initX;
   }
   public BigDecimal screenYToCoords(BigDecimal initY)
   {//y = (a - x + c)/(d*b)
      //adjusting based on screen size
      initY = (yScreenSize.subtract(initY).divide(b, 32, RoundingMode.HALF_UP));// f(initY) = (yScreenSize-initY)/b //y = (a-x)/b
      //adjusting based on pan
      initY = initY.add((panY.divide(tickInterval, 32, RoundingMode.HALF_UP)));//f(initY) = initY + (panY/tickInterval) // y = x + (c/b)
      //adjusting based on zoom
      initY = initY.divide(zoomDynamic, 32, RoundingMode.HALF_UP);//f(initY) = initY/zoomDynamic //y = x/d
      return initY;
   }
}