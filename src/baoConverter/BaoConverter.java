package baoConverter;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Reader;

public class BaoConverter {

	public BaoConverter() {

		File directory = new File("./source");

		generate(directory);
	}


	public static void generate(File directory) {
		System.out.println("Starting Celaria BAO conversion tool");
		System.out.println("Generating BAO files...");
		System.out.println("");

		String lastAnimationName = "";

		// process only .obj files. (as exporting .obj meshes with materials also
		// generates .mtl files which we want to ignore)
		File[] files = directory.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".obj") || name.endsWith(".OBJ");
			}
		});

		FileOutputStream FileStream = null;
		DataOutputStream BinaryStream = null;

		boolean FirstFrameOfModel = true;

		try {
			for (int i = 0; i < files.length; i++) {// loops through all files in the "source" folder.
				File file = files[i]; // get the file

				String path = file.getPath();// get the path of the file
				String fileName = file.getName();// get the name of the file
				
				/*
				 * Animations are expected to be exported with the naming convention "skin_animationName"
				 * like "default_run" or "default_roll" so that multiple skins and animations can be dumped in the source directory. 
				 */
				
				String animationName = fileName.substring(0, fileName.lastIndexOf("_"));// get the animation name of the
																						// file

				int[] information = null;// information of the animation (like vertexcount,trianglecount,etc...)

				if (lastAnimationName.equals(animationName) == false) {// checks if the animation name of the current
																		// file is different from the previous.
					// if that's the case, then it closes the current .bao file and starts a new one
					// of the new animation
					if (FileStream != null) {

						StreamConverter.buffer_write_u8(BinaryStream, 255);// byte 255 means "closing" > no model file
																			// after that.

						// close filestreams
						FileStream.close();
						BinaryStream.close();

					}

					// new animation starts
					FirstFrameOfModel = true;

					lastAnimationName = animationName;// save the current animation name in the "lastAnimationName"
														// variable in order to tell afterwards if a new animation
														// starts.

					FileStream = new FileOutputStream("./converted/" + lastAnimationName + ".bao");// create new
																									// Filestream for
																									// the bao file
					BinaryStream = new DataOutputStream(FileStream);// create a new Binarystream for the binary
																	// information

					System.out.println("- " + lastAnimationName);// prints the animation file name in the console

					information = countInformations(file);// gets information about the animation. Like vertex,
															// trianglecount, if texture coordinates or normals exists,
															// etc..
					startAnimationFile(file, BinaryStream, information);// starts the animation file
				}

				// if that's not the last animation frame of the specific animation, then write
				// a byte with the value "254" in order to tell the software that there is a
				// frame after the current one.
				if (FirstFrameOfModel == false) {// but do this only after the first frame
					StreamConverter.buffer_write_u8(BinaryStream, 254);// byte 254 > another model-frame is following

				}

				writeFrameData(file, BinaryStream);// save the information of one frame into the file.

				FirstFrameOfModel = false;// set to false in order to allow the software to write the "254" bytes after
											// the first frame.

				if (i == files.length - 1) {// if the current file is the last file in the folder > close it.
					if (FileStream != null) {
						// close the file.
						StreamConverter.buffer_write_u8(BinaryStream, 255);// byte 255 means "closing" > no model file
																			// after that.

						// close streams
						FileStream.close();
						BinaryStream.close();

					}

					System.out.println("");
					System.out.println("*finished*");
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * The following function returns the number of vertices,normals,
	 * texturecoodrinates and faces of the given .obj-file.
	 */

	public static int[] countInformations(File source) {
		int triangles = 0;
		int vertices = 0;
		int normals = 0;
		int textureCoordinates = 0;

		try {
			Reader read = new FileReader(source);
			BufferedReader buffer_read = new BufferedReader(read);

			while (buffer_read.ready()) {

				String line = buffer_read.readLine();

				if (line.length() > 0) {// to avoid a crash on empty lines

					if (line.charAt(0) == 'v' && line.charAt(1) == ' ') {
						vertices++;
					}

					if (line.charAt(0) == 'f') {
						// destinationWriter.write(line + "\n");
						triangles++;
					}
					if (line.length() >= 2) {
						if (line.substring(0, 2).equals("vn")) {
							normals++;
						}
						if (line.substring(0, 2).equals("vt")) {
							textureCoordinates++;
						}
					}
				}

			}
			read.close();
			buffer_read.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		int[] arr = { triangles, vertices, textureCoordinates, normals };
		return arr;
	}

	public static void startAnimationFile(File source, DataOutputStream destination, int[] information) {
		try {

			Reader read = new FileReader(source);
			BufferedReader buffer_read = new BufferedReader(read);

			// write header
			StreamConverter.buffer_write_string(destination, "CelariaBAO");// Binary "ID" to check later if that's
																			// really
																			// a .bao file.
			StreamConverter.buffer_write_u8(destination, 1);// revision number (version)

			read.close();
			buffer_read.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void writeFrameData(File source, DataOutputStream destination) {
		try {

			// Get the uv- and normal count from the specific file (as this can change from
			// frame to frame...)
			int[] info = countInformations(source);

			Reader read = new FileReader(source);
			BufferedReader buffer_read = new BufferedReader(read);

			int[] vertNumber = new int[] { 0, 2, 1 };// to change the vertex order

			while (buffer_read.ready()) {

				String line = buffer_read.readLine();

				String[] parts = line.split(" ");
				int parts_length = parts.length;

				switch (parts[0]) {
				case "v":
					StreamConverter.buffer_write_u8(destination, 0);

					// write vertex information
					StreamConverter.buffer_write_f32(destination, Float.parseFloat(parts[parts_length - 3]));// x
					StreamConverter.buffer_write_f32(destination, Float.parseFloat(parts[parts_length - 2]));// y
					StreamConverter.buffer_write_f32(destination, Float.parseFloat(parts[parts_length - 1]));// z

					break;
				case "vn":
					StreamConverter.buffer_write_u8(destination, 2);

					// write normal information
					StreamConverter.buffer_write_f32(destination, Float.parseFloat(parts[1]));// x
					StreamConverter.buffer_write_f32(destination, Float.parseFloat(parts[2]));// y
					StreamConverter.buffer_write_f32(destination, Float.parseFloat(parts[3]));// z

					break;

				case "vt":
					StreamConverter.buffer_write_u8(destination, 1);

					// write uv-information
					StreamConverter.buffer_write_f32(destination, Float.parseFloat(parts[1]));// xt
					StreamConverter.buffer_write_f32(destination, Float.parseFloat(parts[2]));// yt

					break;

				/*
				 * write polygon information (faces)
				 * 
				 * Note: "f" should be always the last information in the obj-files. Make sure
				 * that the exported OBJ-files don't have the face-information "f ..." scattered
				 * around in the file and mixed with other information.
				 * 
				 * So as an example, a valid obj file would be:
				 * ------------------------------------- v ... v ... v ... vt ... vt ... vt ...
				 * vn ... vn ... vn ... f ... f ... f ... -----------------------------
				 */

				case "f":
					StreamConverter.buffer_write_u8(destination, 3);

					// one polygon consists of 3 vertices
					for (int g = 0; g < 3; g++) {// write 3 vertices from the obj-file into the .bao file
						String[] vi = parts[vertNumber[g] + 1].split("/");

						String v = vi[0];
						String t = vi[1];
						String n = vi[2];

						// write default-values for the normals and uv-coordinates if they don't exist.
						if (t.equals("")) {
							t = "0";
						}
						;
						if (n.equals("")) {
							n = "0";
						}
						;

						// write the vertex information for the polygon
						StreamConverter.buffer_write_u32(destination, Integer.parseInt(v));// vertex nr
						StreamConverter.buffer_write_u32(destination, Integer.parseInt(t));// texture nr
						StreamConverter.buffer_write_u32(destination, Integer.parseInt(n));// normal nr
					}

					break;

				case "usemtl":
					StreamConverter.buffer_write_u8(destination, 4);

					String baseNameMaterial = parts[1];
					if (baseNameMaterial.contains(".")) {
						baseNameMaterial = baseNameMaterial.split(".")[0];// remove variant
					}

					char[] buffer = baseNameMaterial.toCharArray();
					StreamConverter.buffer_write_u8(destination, (int) buffer.length);
					for (int i = 0; i < buffer.length; i++) {
						StreamConverter.buffer_write_u8(destination, (int) buffer[i]);

					}

					break;

				case "o":
					StreamConverter.buffer_write_u8(destination, 5);

					String baseNameObject = parts[1];
					if (baseNameObject.contains(".")) {
						baseNameObject = baseNameObject.split(".")[0];// remove variant
					}

					char[] buffer_o = baseNameObject.toCharArray();
					StreamConverter.buffer_write_u8(destination, (int) buffer_o.length);
					for (int i = 0; i < buffer_o.length; i++) {
						StreamConverter.buffer_write_u8(destination, (int) buffer_o[i]);
					}
					break;

				}

			}

			read.close();
			buffer_read.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}