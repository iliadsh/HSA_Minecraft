import java.awt.*;
import hsa.Console;
import java.util.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.swing.*;

public class ThreeDEngine
{
    static Console c;
    static BufferedImage gc;
    static Graphics2D g;
    static Mesh cube;
    static Mat4x4 matProj;
    static float aspectRatio;
    static float fov = 90;
    static float zFar = 1000;
    static float zNear = 0.1f;
    static Random r = new Random ();
    static float movex, movey, movez = 0;
    static InputLoop loop;
    static Vector3 cam = new Vector3 (0, 0, 0);
    static Vector3 lookDir = new Vector3();
    static float camyaw = 0;
    static float campitch = 0;
    static float thetax = 0;
    static float thetaz = 0;
    static float thetay = 0;
    static boolean showWireFrame = false;


    public static void updateProjectionMat ()
    {
    	 matProj = Mat4x4.makeProjection(fov, aspectRatio, zNear, zFar);
    }
    


    public static void main (String[] args)
    {
        c = new Console ();
        gc = new BufferedImage (c.getWidth (), c.getHeight (), BufferedImage.TYPE_INT_ARGB);
        g = gc.createGraphics ();
        loop = new InputLoop ();
        loop.start ();
        cube = new Mesh ();
        matProj = new Mat4x4 ();
        aspectRatio = (float) c.getHeight () / (float) c.getWidth ();

        updateProjectionMat();
       

        //cube.loadFromFile ("teapot.obj");

        cube.tris = new ArrayList();

        //SOUTH
        cube.tris.add(new Triangle(new Vector3(0, 0, 0), new Vector3(0, 1, 0), new Vector3(1, 1, 0), Color.green));
        cube.tris.add(new Triangle(new Vector3(0, 0, 0), new Vector3(1, 1, 0), new Vector3(1, 0, 0), Color.green));
        //EAST
        cube.tris.add(new Triangle(new Vector3(1, 0, 0), new Vector3(1, 1, 0), new Vector3(1, 1, 1), Color.green));
        cube.tris.add(new Triangle(new Vector3(1, 0, 0), new Vector3(1, 1, 1), new Vector3(1, 0, 1), Color.green));
        //NORTH
        cube.tris.add(new Triangle(new Vector3(1, 0, 1), new Vector3(1, 1, 1), new Vector3(0, 1, 1), Color.green));
        cube.tris.add(new Triangle(new Vector3(1, 0, 1), new Vector3(0, 1, 1), new Vector3(0, 0, 1), Color.green));
        //WEST
        cube.tris.add(new Triangle(new Vector3(0, 0, 1), new Vector3(0, 1, 1), new Vector3(0, 1, 0), Color.green));
        cube.tris.add(new Triangle(new Vector3(0, 0, 1), new Vector3(0, 1, 0), new Vector3(0, 0, 0), Color.green));
        //TOP
        cube.tris.add(new Triangle(new Vector3(0, 1, 0), new Vector3(0, 1, 1), new Vector3(1, 1, 1), Color.green));
        cube.tris.add(new Triangle(new Vector3(0, 1, 0), new Vector3(1, 1, 1), new Vector3(1, 1, 0), Color.green));
        //BOTTOM
        cube.tris.add(new Triangle(new Vector3(1, 0, 1), new Vector3(0, 0, 1), new Vector3(0, 0, 0), Color.green));
        cube.tris.add(new Triangle(new Vector3(1, 0, 1), new Vector3(0, 0, 0), new Vector3(1, 0, 0), Color.green));

        while (true)
        {
            clear (g);
            g.setColor (Color.black);

            Mat4x4 matRotZ = new Mat4x4 (), matRotX = new Mat4x4 (), matRotY = new Mat4x4(), matTrans = new Mat4x4(), matWorld = new Mat4x4();

            // Rotation Z
            matRotZ = Mat4x4.makeRotationZ(thetaz);

            // Rotation X
            matRotX = Mat4x4.makeRotationX(thetax * 0.5f);
            
            // Rotation Y
            matRotY = Mat4x4.makeRotationY(thetay);
            
            // Translation
            matTrans = Mat4x4.makeTranslation(0 + movex, 0 + movey, 3 + movez);
            
            // Matworld
            matWorld = Mat4x4.makeIdentity();
            matWorld = Mat4x4.multiplyMatrix(matRotZ, matRotX);
            matWorld = Mat4x4.multiplyMatrix(matWorld, matRotY);
            matWorld = Mat4x4.multiplyMatrix(matWorld, matTrans);
            
            Vector3 up = new Vector3(0, 1, 0);
            Vector3 target = new Vector3(0, 0, 1);
            Mat4x4 matCameraRotY = Mat4x4.makeRotationY(camyaw);
            Mat4x4 matCameraRotX = Mat4x4.makeRotationX(campitch * 0.5f);
            Mat4x4 matCameraRot = Mat4x4.multiplyMatrix(matCameraRotY, matCameraRotX);
            lookDir = Vector3.MultiplyMatrixVector(target, matCameraRot);
            target = Vector3.add(cam, lookDir);
            
            Mat4x4 matCamera = Mat4x4.pointAt(cam, target, up);
            
            Mat4x4 matView = Mat4x4.quickInverse(matCamera);
            

            ArrayList drawTris = new ArrayList ();

            for (Iterator rowIterator = cube.tris.iterator () ; rowIterator.hasNext () ;)
            {
                Triangle row = (Triangle) rowIterator.next ();

                Triangle triProjected = new Triangle ();
                Triangle triTransformed = new Triangle ();
                Triangle triViewed = new Triangle();

                triTransformed.p[0] = Vector3.MultiplyMatrixVector(row.p[0], matWorld);
                triTransformed.p[1] = Vector3.MultiplyMatrixVector(row.p[1], matWorld);
                triTransformed.p[2] = Vector3.MultiplyMatrixVector(row.p[2], matWorld);

                Vector3 normal = new Vector3 (), line1 = new Vector3 (), line2 = new Vector3 ();

                line1 = Vector3.sub(triTransformed.p[1], triTransformed.p[0]);
                line2 = Vector3.sub(triTransformed.p[2], triTransformed.p[0]);
                
                normal = Vector3.crossProduct(line1, line2);
                
                normal = Vector3.normalise(normal);

                
                Vector3 vCamRay = Vector3.sub(triTransformed.p[0], cam);
                
                triViewed.p[0] = Vector3.MultiplyMatrixVector(triTransformed.p[0], matView);
                triViewed.p[1] = Vector3.MultiplyMatrixVector(triTransformed.p[1], matView);
                triViewed.p[2] = Vector3.MultiplyMatrixVector(triTransformed.p[2], matView);

                if (Vector3.dotProduct(normal, vCamRay) < 0 && triViewed.p [0].z > 0 && triViewed.p [1].z > 0 && triViewed.p [2].z > 0)
                {

                    Vector3 light_direction = new Vector3 (0, -1, -1);
                    light_direction = Vector3.normalise(light_direction);
                    
                    float dp = Vector3.dotProduct(light_direction, normal);

                    Color c1 = getShadedColor (row.color, dp);

                    triProjected.p [0] = Vector3.MultiplyMatrixVector (triViewed.p [0], matProj);
                    triProjected.p [1] = Vector3.MultiplyMatrixVector (triViewed.p [1], matProj);
                    triProjected.p [2] = Vector3.MultiplyMatrixVector (triViewed.p [2], matProj);
                    triProjected.color = c1;
                    
                    
                    if (triProjected.p[0].w != 0 && triProjected.p[1].w != 0 && triProjected.p[2].w !=0) {
                    	triProjected.p[0] = Vector3.div(triProjected.p[0], triProjected.p[0].w);
                        triProjected.p[1] = Vector3.div(triProjected.p[1], triProjected.p[1].w);
                        triProjected.p[2] = Vector3.div(triProjected.p[2], triProjected.p[2].w);
                    }
                    
                    

                    Vector3 offset = new Vector3(1, 1, 0);
                    
                    triProjected.p [0] = Vector3.add(triProjected.p[0], offset);
                    triProjected.p [1] = Vector3.add(triProjected.p[1], offset);
                    triProjected.p [2] = Vector3.add(triProjected.p[2], offset);

                    triProjected.p [0].x *= 0.5 * (float) c.getWidth ();
                    triProjected.p [0].y *= 0.5 * (float) c.getHeight ();
                    triProjected.p [1].x *= 0.5 * (float) c.getWidth ();
                    triProjected.p [1].y *= 0.5 * (float) c.getHeight ();
                    triProjected.p [2].x *= 0.5 * (float) c.getWidth ();
                    triProjected.p [2].y *= 0.5 * (float) c.getHeight ();


                    drawTris.add (triProjected);

                }

            }

            Collections.sort (drawTris, new Sortbydistance ());

            for (Iterator rowIterator = drawTris.iterator () ; rowIterator.hasNext () ;)
            {
                Triangle triProjected = (Triangle) rowIterator.next ();
                int[] xs = new int [3];
                xs [0] = (int) triProjected.p [0].x;
                xs [1] = (int) triProjected.p [1].x;
                xs [2] = (int) triProjected.p [2].x;
                int[] ys = new int [3];
                ys [0] = (int) triProjected.p [0].y;
                ys [1] = (int) triProjected.p [1].y;
                ys [2] = (int) triProjected.p [2].y;
                g.setColor (triProjected.color);
                g.fillPolygon (xs, ys, ys.length);
                g.setColor (Color.black);
                if (showWireFrame)
                    g.drawPolygon (xs, ys, ys.length);
            }

            c.drawImage (gc, 0, 0, null);
        }

    }


    public static void clear (Graphics2D g)
    {
        g.setColor (new Color(0, 200, 255));
        g.fillRect (0, 0, c.getWidth (), c.getHeight ());
        g.setColor (Color.black);
    }


    public static Color getShadedColor (Color oc, float dp)
    {
        Color output = oc;
        int pixel_bw = (int) (13 * dp);
        switch (pixel_bw)
        {
            case 0:
                output = oc.darker ().darker ().darker ().darker ().darker ();
                break;

            case 4:
                output = oc.darker ();
                break;
            case 3:
                output = oc.darker ().darker ();
                break;
            case 2:
                output = oc.darker ().darker ().darker ();
                break;
            case 1:
                output = oc.darker ().darker ().darker ().darker ();
                break;

            case 5:
                output = oc.brighter ();
                break;
            case 6:
                output = oc.brighter ().brighter ();
                break;
            case 7:
                output = oc.brighter ().brighter ().brighter ();
                break;
            case 8:
                output = oc.brighter ().brighter ().brighter ().brighter ();
                break;

            case 9:
                output = oc.brighter ().brighter ().brighter ().brighter ().brighter ();
                break;
            case 10:
                output = oc.brighter ().brighter ().brighter ().brighter ().brighter ().brighter ();
                break;
            case 11:
                output = oc.brighter ().brighter ().brighter ().brighter ().brighter ().brighter ().brighter ();
                break;
            case 12:
                output = oc.brighter ().brighter ().brighter ().brighter ().brighter ().brighter ().brighter ().brighter ();
                break;
            default:
                output = oc.brighter ().brighter ().brighter ().brighter ().brighter ().brighter ().brighter ().brighter ().brighter ();
        }
        return output;
    }


    
}

class Vector3
{
    public Vector3 ()
    {
        this.x = 0;
        this.y = 0;
        this.z = 0;
        this.w = 1;
    }


    public Vector3 (float x, float y, float z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = 1;
    }
    
    public static Vector3 add(Vector3 v1, Vector3 v2) {
        return new Vector3(v1.x + v2.x, v1.y + v2.y, v1.z + v2.z);
    }
    
    public static Vector3 sub(Vector3 v1, Vector3 v2) {
        return new Vector3(v1.x - v2.x, v1.y - v2.y, v1.z - v2.z);
    }
    
    public static Vector3 mul(Vector3 v1, float k) {
        return new Vector3(v1.x * k, v1.y * k, v1.z * k);
    }

    public static Vector3 div(Vector3 v1, float k) {
        return new Vector3(v1.x / k, v1.y / k, v1.z / k);
    }
    
    public static float dotProduct(Vector3 v1, Vector3 v2) {
        return v1.x * v2.x + v1.y * v2.y + v1.z * v2.z;
    }
    
    public static float length(Vector3 v1) {
        return (float)Math.sqrt(Vector3.dotProduct(v1, v1));
    }
    
    public static Vector3 normalise(Vector3 v1) {
        float l = Vector3.length(v1);
        return new Vector3(v1.x / l, v1.y / l, v1.z / l);
    }
    
    public static Vector3 crossProduct(Vector3 v1, Vector3 v2) {
        Vector3 output = new Vector3();
        output.x = v1.y * v2.z - v1.z * v2.y;
        output.y = v1.z * v2.x - v1.x * v2.z;
        output.z = v1.x * v2.y - v1.y * v2.x;
        return output;
    }
    
    public static Vector3 MultiplyMatrixVector (Vector3 i, Mat4x4 m)
    {
        Vector3 o = new Vector3 ();
        o.x = i.x * m.m [0][0] + i.y * m.m [1][0] + i.z * m.m [2][0] + i.w * m.m [3] [0];
        o.y = i.x * m.m [0][1] + i.y * m.m [1][1] + i.z * m.m [2][1] + i.w * m.m [3] [1];
        o.z = i.x * m.m [0][2] + i.y * m.m [1][2] + i.z * m.m [2][2] + i.w * m.m [3] [2];
        o.w = i.x * m.m [0][3] + i.y * m.m [1][3] + i.z * m.m [2][3] + i.w * m.m [3] [3];
        return o;
    }
    

    float x, y, z, w;
}

class Triangle
{
    public Triangle ()
    {
        p [0] = new Vector3 ();
        p [1] = new Vector3 ();
        p [2] = new Vector3 ();
        this.color = Color.black;
    }


    public Triangle (Vector3 v1, Vector3 v2, Vector3 v3)
    {
        p [0] = v1;
        p [1] = v2;
        p [2] = v3;
        this.color = Color.black;
    }


    public Triangle (Vector3 v1, Vector3 v2, Vector3 v3, Color color)
    {
        p [0] = v1;
        p [1] = v2;
        p [2] = v3;
        this.color = color;
    }


    Vector3[] p = new Vector3 [3];
    public Color color;
}

class Mesh
{
    ArrayList tris = new ArrayList ();

    public void loadFromFile (String file)
    {
        BufferedReader reader;
        ArrayList verts = new ArrayList ();
        try
        {
            reader = new BufferedReader (new FileReader (file));
            String line = reader.readLine ();
            while (line != null)
            {
                //System.out.println(line);
                // read next line
                String[] data = line.split (" ");
                if (data [0].equals ("v"))
                {
                    verts.add (new Vector3 (Float.parseFloat (data [1]), Float.parseFloat (data [2]), Float.parseFloat (data [3])));
                }
                if (data [0].equals ("f"))
                {
                    int[] f = new int [3];
                    if (data [1].toLowerCase ().matches ("(?i).*/*"))
                    {
                        data [1] = data [1].split ("/") [0];
                        data [2] = data [2].split ("/") [0];
                        data [3] = data [3].split ("/") [0];
                    }

                    f [0] = Integer.parseInt (data [1]) - 1;
                    f [1] = Integer.parseInt (data [2]) - 1;
                    f [2] = Integer.parseInt (data [3]) - 1;
                    tris.add (new Triangle ((Vector3) verts.get (f [0]), (Vector3) verts.get (f [1]), (Vector3) verts.get (f [2]), Color.green));
                }
                line = reader.readLine ();
            }
            reader.close ();
        }
        catch (IOException e)
        {
            e.printStackTrace ();
        }
    }
}

class Mat4x4
{
    float[] [] m = new float [4] [4];
    
    public static Mat4x4 makeIdentity() {
    	Mat4x4 out = new Mat4x4();
    	out.m[0][0] = 1;
    	out.m[1][1] = 1;
    	out.m[2][2] = 1;
    	out.m[3][3] = 1;
    	return out;
    }
    public static Mat4x4 makeRotationX(float fAngleRad)
	{
		Mat4x4 matrix = new Mat4x4();
		matrix.m[0][0] = 1.0f;
		matrix.m[1][1] = (float)Math.cos(fAngleRad);
		matrix.m[1][2] = (float)Math.sin(fAngleRad);
		matrix.m[2][1] = (float) - Math.sin(fAngleRad);
		matrix.m[2][2] = (float)Math.cos(fAngleRad);
		matrix.m[3][3] = 1.0f;
		return matrix;
	}

	public static Mat4x4 makeRotationY(float fAngleRad)
	{
		Mat4x4 matrix = new Mat4x4();
		matrix.m[0][0] = (float)Math.cos(fAngleRad);
		matrix.m[0][2] = (float)Math.sin(fAngleRad);
		matrix.m[2][0] = (float) - Math.sin(fAngleRad);
		matrix.m[1][1] = 1.0f;
		matrix.m[2][2] = (float)Math.cos(fAngleRad);
		matrix.m[3][3] = 1.0f;
		return matrix;
	}

	public static Mat4x4 makeRotationZ(float fAngleRad)
	{
		Mat4x4 matrix = new Mat4x4();
		matrix.m[0][0] = (float)Math.cos(fAngleRad);
		matrix.m[0][1] = (float)Math.sin(fAngleRad);
		matrix.m[1][0] = (float) - Math.sin(fAngleRad);
		matrix.m[1][1] = (float)Math.cos(fAngleRad);
		matrix.m[2][2] = 1.0f;
		matrix.m[3][3] = 1.0f;
		return matrix;
	}
	
	public static Mat4x4 makeTranslation(float x, float y, float z)
	{
		Mat4x4 matrix  = new Mat4x4();
		matrix.m[0][0] = 1.0f;
		matrix.m[1][1] = 1.0f;
		matrix.m[2][2] = 1.0f;
		matrix.m[3][3] = 1.0f;
		matrix.m[3][0] = x;
		matrix.m[3][1] = y;
		matrix.m[3][2] = z;
		return matrix;
	}

	public static Mat4x4 makeProjection(float fFovDegrees, float fAspectRatio, float fNear, float fFar)
	{
		float fFovRad = (float)(1.0f / Math.tan(fFovDegrees * 0.5f / 180.0f * Math.PI));
		Mat4x4 matrix  = new Mat4x4();
		matrix.m[0][0] = fAspectRatio * fFovRad;
		matrix.m[1][1] = -fFovRad;
		matrix.m[2][2] = fFar / (fFar - fNear);
		matrix.m[3][2] = (-fFar * fNear) / (fFar - fNear);
		matrix.m[2][3] = 1.0f;
		matrix.m[3][3] = 0.0f;
		return matrix;
	}

	public static Mat4x4 multiplyMatrix(Mat4x4 m1, Mat4x4 m2)
	{
		Mat4x4 matrix = new Mat4x4();
		for (int c = 0; c < 4; c++)
		{
			for (int r = 0; r < 4; r++)
				matrix.m[r][c] = m1.m[r][0] * m2.m[0][c] + m1.m[r][1] * m2.m[1][c] + m1.m[r][2] * m2.m[2][c] + m1.m[r][3] * m2.m[3][c];
		}
		return matrix;
	}
	
	public static Mat4x4 pointAt(Vector3 pos, Vector3 target, Vector3 up) 
	{
		Vector3 newForward = Vector3.sub(target, pos);
		newForward = Vector3.normalise(newForward);

		Vector3 a = Vector3.mul(newForward, Vector3.dotProduct(up, newForward));
		Vector3 newUp = Vector3.sub(up, a);
		newUp = Vector3.normalise(newUp);

		Vector3 newRight = Vector3.crossProduct(newUp, newForward);

		Mat4x4 matrix = new Mat4x4();
		matrix.m[0][0] = newRight.x;	matrix.m[0][1] = newRight.y;	matrix.m[0][2] = newRight.z;	matrix.m[0][3] = 0.0f;
		matrix.m[1][0] = newUp.x;		matrix.m[1][1] = newUp.y;		matrix.m[1][2] = newUp.z;		matrix.m[1][3] = 0.0f;
		matrix.m[2][0] = newForward.x;	matrix.m[2][1] = newForward.y;	matrix.m[2][2] = newForward.z;	matrix.m[2][3] = 0.0f;
		matrix.m[3][0] = pos.x;			matrix.m[3][1] = pos.y;			matrix.m[3][2] = pos.z;			matrix.m[3][3] = 1.0f;
		return matrix;
	}
	
	public static Mat4x4 quickInverse(Mat4x4 m) 
	{
		Mat4x4 matrix = new Mat4x4();
		matrix.m[0][0] = m.m[0][0]; matrix.m[0][1] = m.m[1][0]; matrix.m[0][2] = m.m[2][0]; matrix.m[0][3] = 0.0f;
		matrix.m[1][0] = m.m[0][1]; matrix.m[1][1] = m.m[1][1]; matrix.m[1][2] = m.m[2][1]; matrix.m[1][3] = 0.0f;
		matrix.m[2][0] = m.m[0][2]; matrix.m[2][1] = m.m[1][2]; matrix.m[2][2] = m.m[2][2]; matrix.m[2][3] = 0.0f;
		matrix.m[3][0] = -(m.m[3][0] * matrix.m[0][0] + m.m[3][1] * matrix.m[1][0] + m.m[3][2] * matrix.m[2][0]);
		matrix.m[3][1] = -(m.m[3][0] * matrix.m[0][1] + m.m[3][1] * matrix.m[1][1] + m.m[3][2] * matrix.m[2][1]);
		matrix.m[3][2] = -(m.m[3][0] * matrix.m[0][2] + m.m[3][1] * matrix.m[1][2] + m.m[3][2] * matrix.m[2][2]);
		matrix.m[3][3] = 1.0f;
		return matrix;
	}
	
}

class Sortbydistance implements Comparator
{

    public int compare (Object t1, Object t2)
    {
        Triangle tri1 = (Triangle) t1;
        Triangle tri2 = (Triangle) t2;
        float tri1av = (tri1.p [0].z + tri1.p [1].z + tri1.p [2].z) / 3;
        float tri2av = (tri2.p [0].z + tri2.p [1].z + tri2.p [2].z) / 3;
        int result = 0;
        if (tri1av > tri2av)
            result = -1;
        if (tri1av < tri2av)
            result = 1;
        return result;
    }
}

class InputLoop implements Runnable
{
    private Thread t;
    public static char CurrentKey;


    public void run ()
    {
        while (true)
        {
            CurrentKey = ThreeDEngine.c.getChar ();
            if (CurrentKey == 'q') {
            	ThreeDEngine.cam.y -= 0.1f;
            }
            if (CurrentKey == 'e') {
            	ThreeDEngine.cam.y += 0.1f;
            }
            
            Vector3 forward = Vector3.mul(ThreeDEngine.lookDir, 0.1f);
            
            Vector3 a = Vector3.mul(forward, Vector3.dotProduct(new Vector3(0, 1, 0), forward));
    		Vector3 newUp = Vector3.sub(new Vector3(0, 1, 0), a);
    		newUp = Vector3.normalise(newUp);

    		Vector3 newRight = Vector3.crossProduct(newUp, forward);
            
            if (CurrentKey == 'w') {
            	ThreeDEngine.cam = Vector3.add(ThreeDEngine.cam, forward);
            
            }
            if (CurrentKey == 's') {
            	ThreeDEngine.cam = Vector3.sub(ThreeDEngine.cam, forward);
            
            }
            if (CurrentKey == 'd') {
            	ThreeDEngine.cam = Vector3.add(ThreeDEngine.cam, newRight);
            
            }
            if (CurrentKey == 'a') {
            	ThreeDEngine.cam = Vector3.sub(ThreeDEngine.cam, newRight);
            
            }
            if (CurrentKey == 'j') {
            	ThreeDEngine.camyaw += 0.1f;
            }
            if (CurrentKey == 'l') {
            	ThreeDEngine.camyaw -= 0.1f;
            }
            if (CurrentKey == 'i') {
            	ThreeDEngine.campitch -= 0.1f;
            }
            if (CurrentKey == 'k') {
            	ThreeDEngine.campitch += 0.1f;
            }
            if (CurrentKey == 't')
            {
                ThreeDEngine.showWireFrame = !ThreeDEngine.showWireFrame;
            }
        }
    }


    public void start ()
    {
        if (t == null)
        {
            t = new Thread (this, "InputLoop");
            t.start ();
        }
    }
}


