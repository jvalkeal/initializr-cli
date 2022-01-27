/*
 * Copyright 2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.experimental.initializrcli;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.sun.jna.Structure;
@SpringBootApplication
public class InitializrCliApplication {

    // class termios extends Structure {

    //     public int c_iflag;
    //     public int c_oflag;
    //     public int c_cflag;
    //     public int c_lflag;
    //     public byte c_line;
    //     public byte[] c_cc = new byte[32];
    //     public int c_ispeed;
    //     public int c_ospeed;

    //     @Override
    //     protected List<String> getFieldOrder() {
    //         return Arrays.asList(//
    //                 "c_iflag",//
    //                 "c_oflag",//
    //                 "c_cflag",//
    //                 "c_lflag",//
    //                 "c_line",//
    //                 "c_cc",//
    //                 "c_ispeed",//
    //                 "c_ospeed"//
    //         );
    //     }
	// }

	// static List<Field> getFieldList() {
	// 	List<Field> flist = new ArrayList<Field>();
	// 	for (Class<?> cls = termios.class; !cls.equals(Structure.class); cls = cls.getSuperclass()) {
	// 		System.out.println("DDD1 " + cls);
	// 		List<Field> classFields = new ArrayList<Field>();
	// 		Field[] fields = cls.getDeclaredFields();
	// 		System.out.println("DDD21 " + fields);
	// 		System.out.println("DDD22 " + fields.length);
	// 		for (int i = 0; i < fields.length; i++) {
	// 			int modifiers = fields[i].getModifiers();
	// 			System.out.println("DDD3 " + modifiers);
	// 			if (Modifier.isStatic(modifiers) || !Modifier.isPublic(modifiers)) {
	// 				System.out.println("DDD4 ");
	// 				continue;
	// 			}
	// 			System.out.println("DDD5 " + fields[i]);
	// 			classFields.add(fields[i]);
	// 		}
	// 		flist.addAll(0, classFields);
	// 	}
	// 	return flist;
	// }

	public static void main(String[] args) {
		// List<Field> fieldList = getFieldList();
		// System.out.println("XXX " + fieldList);
		SpringApplication.run(InitializrCliApplication.class, args);
	}
}
