/*
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hyperledger.fabric.samples.fabcar;


import java.io.UnsupportedEncodingException;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;


import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Contact;
import org.hyperledger.fabric.contract.annotation.License;
import org.hyperledger.fabric.contract.annotation.Info;
import org.hyperledger.fabric.contract.annotation.Default;
import org.hyperledger.fabric.contract.annotation.Transaction;


import org.hyperledger.fabric.shim.ChaincodeException;
import org.hyperledger.fabric.shim.ChaincodeStub;


import com.owlike.genson.Genson;



@Contract(
        name = "FabCar",
        info = @Info(
                title = "FabCar contract",
                description = "The hyperlegendary car contract",
                version = "0.0.1-SNAPSHOT",
                license = @License(
                        name = "Apache 2.0 License",
                        url = "http://www.apache.org/licenses/LICENSE-2.0.html"),
                contact = @Contact(
                        email = "f.carr@example.com",
                        name = "F Carr",
                        url = "https://hyperledger.example.com")))

@Default
public final class FabCar implements ContractInterface {

    private final Genson genson = new Genson();

    public static int gbfInit(final int dataCount) {
        long bitSize = dataCount;
        if (bitSize < 0 || bitSize > Integer.MAX_VALUE) {
            throw new RuntimeException("位数太大溢出了，请降低降低数据大小");
        }
        int abfSize = (int) bitSize * dataCount; //可修改，越大越不会产生碰撞
        return abfSize;
    }

    public static HashMap addArray(final String[] dataArray, final int size) {
        final int attlenth = 3;
        final int numlength = 3;
        final int tmp = 3;
        HashMap<Integer, String> hashMap = new HashMap<Integer, String>();
        for (int k = 0; k < dataArray.length; k++) {
            String data = dataArray[k];
            String rownum = String.valueOf(k);
            int initIndex = -1;

            int[] index = new int[tmp];
            index[0] = djbHash(data) % size;
            index[1] = bkdrHash(data) % size;
            index[2] = jsHash(data) % size;
            //标准化长度，下面所有生成的随机字符串长度都为numlength+attlength
            while (data.length() < attlenth) {
                data = "0" + data;
            }
            while (rownum.length() < numlength) {
                rownum = "0" + rownum;
            }
            data = rownum + data;

            String finalShare = data; //这里初始设置为data
            for (int i = 0; i < tmp; i++) {
                if (!hashMap.containsKey(index[i])) {
                    if (initIndex == -1) {
                        initIndex = index[i];
                    } else {
                        hashMap.put(index[i], getRandomString(numlength + attlenth));
                        finalShare = twoStringXor(finalShare, (String) hashMap.get(index[i]));
                    }
                } else {
                    finalShare = twoStringXor(data, (String) hashMap.get(index[i]));
                }
            }
            hashMap.put(initIndex, finalShare);
        }
        /*填充剩余的位置为随机数*/
        for (int i = 0; i < size; i++) {
            if (!hashMap.containsKey(i)) {
                hashMap.put(i, getRandomString(numlength + attlenth));
            }
        }
        return hashMap;
    }

    public static String getRandomString(final int length) {
        final String str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        final int tmp = 62;
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(tmp);
            sb.append(str.charAt(number));
        }
        return sb.toString();
    }

    public static String getRho(final int exnum) {
        //att 10, 20, 30, 40, 50
        String[] rhoStringvec = {"j,g,a,h,d,b,c,e,f,i",
                "d1,h,j1,a,c1,f,g,h1,e,b1,f1,c,a1,j,e1,b,g1,d,i1,i",
                "d,h1,e1,i,f1,g,a2,j1,g2,f,i1,f2,b,c1,b2,e,i2,e2,d2,c2,j,b1,d1,g1,c,a,h,h2,j2,a1",
        "j2,h,f2,b3,g,c,f1,g2,j,e1,i3,c2,b1,h2,b,f,a3,g3,b2,h3,a1,h1,i1,i,a,d2,d3,e3,c3,d,g1,a2,j3,e2,e,c1,i2,j1,d1,f3",
        "f1,a2,j4,j1,i2,a,c2,d2,i1,h4,b1,j3,g2,i4,b4,g1,c,e2,h,b2,a4,b,f3,d3,c4,c3,e,a1,e1,d4,h1,f,g3,g,i3,h3,d,f2,b3,"
                        + "g4,a3,j,c1,h2,e4,d1,j2,f4,i,e3"};
        return rhoStringvec[exnum];
    }

    public static String getMString(final int exnum) {
        String[] mStringvec = {"[10, 4, 1, 2, 0, 625, 1, 2, 0, 16, 1, 1, 1, 0, 1, 2, 0, 81, 1, 1, 256, 0, 1, 1, 16, 0, "
        + "1, 1, 81, 0, 1, 1, 625, 0, 1, 2, 0, 1, 1, 2, 0, 256]",
        "[20, 4, 1, 2, 0, 262144, 1, 1, 134217728, 0, 1, 2, 0, 1000000000, 1, 1, 1, 0, 1, 2, 0, 19683, 1, 1, 10077696, "
        + "0, 1, 1, 40353607, 0, 1, 2, 0, 134217728, 1, 1, 1953125, 0, 1, 2, 0, 512, 1, 2, 0, 10077696, 1, 1, 19683, "
        + "0, 1, 2, 0, 1, 1, 1, 1000000000, 0, 1, 2, 0, 1953125, 1, 1, 512, 0, 1, 2, 0, 40353607, 1, 1, 262144, 0, 1, "
        + "2, 0, 387420489, 1, 1, 387420489, 0]",
        "[30, 4, 1, 1, 2147483647, 0, 1, 1, 2147483647, 0, 1, 1, 2147483647, 0, 1, 1, 2147483647, 0, 1, 1, 2147483647, "
        + "0, 1, 1, 2147483647, 0, 1, 2, 0, 1, 1, 1, 2147483647, 0, 1, 2, 0, 40353607, 1, 1, 2147483647, 0, 1, 1, "
        + "2147483647, 0, 1, 2, 0, 10077696, 1, 1, 524288, 0, 1, 1, 2147483647, 0, 1, 2, 0, 512, 1, 1, 2147483647, 0, "
        + "1, 2, 0, 387420489, 1, 2, 0, 1953125, 1, 2, 0, 262144, 1, 2, 0, 19683, 1, 1, 2147483647, 0, 1, 1, "
        + "2147483647, 0, 1, 1, 2147483647, 0, 1, 1, 2147483647, 0, 1, 1, 1162261467, 0, 1, 1, 1, 0, 1, 1, 2147483647, "
        + "0, 1, 2, 0, 134217728, 1, 2, 0, 1000000000, 1, 1, 2147483647, 0]",
        "[40, 4, 1, 1, 2147483647, 0, 1, 1, 2147483647, 0, 1, 1, 2147483647, 0, 1, 2, 0, 512, 1, 1, 2147483647, 0, 1, "
        + "1, 2147483647, 0, 1, 1, 2147483647, 0, 1, 1, 2147483647, 0, 1, 1, 2147483647, 0, 1, 1, 2147483647, 0, 1, 2, "
        + "0, 387420489, 1, 1, 2147483647, 0, 1, 1, 2147483647, 0, 1, 1, 2147483647, 0, 1, 1, 536870912, 0, 1, 1, "
        + "2147483647, 0, 1, 2, 0, 1, 1, 2, 0, 40353607, 1, 1, 2147483647, 0, 1, 2, 0, 134217728, 1, 1, 2147483647, 0, "
        + "1, 1, 2147483647, 0, 1, 1, 2147483647, 0, 1, 1, 2147483647, 0, 1, 1, 1, 0, 1, 1, 2147483647, 0, 1, 2, 0, "
        + "262144, 1, 2, 0, 1953125, 1, 2, 0, 19683, 1, 1, 2147483647, 0, 1, 1, 2147483647, 0, 1, 1, 2147483647, 0, 1, "
        + "2, 0, 1000000000, 1, 1, 2147483647, 0, 1, 1, 2147483647, 0, 1, 1, 2147483647, 0, 1, 1, 2147483647, 0, 1, 1, "
        + "2147483647, 0, 1, 1, 2147483647, 0, 1, 2, 0, 10077696]",
        "[50, 4, 1, 1, 2147483647, 0, 1, 1, 2147483647, 0, 1, 2, 0, 1000000000, 1, 1, 2147483647, 0, 1, 1, 2147483647, "
        + "0, 1, 1, 1, 0, 1, 1, 2147483647, 0, 1, 1, 2147483647, 0, 1, 1, 2147483647, 0, 1, 2, 0, 134217728, 1, 1, "
        + "2147483647, 0, 1, 1, 2147483647, 0, 1, 1, 2147483647, 0, 1, 2, 0, 387420489, 1, 2, 0, 512, 1, 1, "
        + "2147483647, 0, 1, 1, 2147483647, 0, 1, 1, 2147483647, 0, 1, 1, 2147483647, 0, 1, 1, 2147483647, 0, 1, 2, 0, "
        + "1, 1, 1, 2147483647, 0, 1, 1, 2147483647, 0, 1, 1, 2147483647, 0, 1, 2, 0, 19683, 1, 1, 2147483647, 0, 1, "
        + "1, 2147483647, 0, 1, 1, 2147483647, 0, 1, 1, 2147483647, 0, 1, 2, 0, 262144, 1, 1, 2147483647, 0, 1, 1, "
        + "2147483647, 0, 1, 1, 2147483647, 0, 1, 1, 2147483647, 0, 1, 1, 2147483647, 0, 1, 1, 2147483647, 0, 1, 1, "
        + "2147483647, 0, 1, 1, 2147483647, 0, 1, 1, 2147483647, 0, 1, 2, 0, 40353607, 1, 1, 2147483647, 0, 1, 1, "
        + "2147483647, 0, 1, 1, 2147483647, 0, 1, 1, 2147483647, 0, 1, 2, 0, 1953125, 1, 1, 2147483647, 0, 1, 1, "
        + "2147483647, 0, 1, 2, 0, 10077696, 1, 1, 2147483647, 0, 1, 1, 2147483647, 0]"};
        String mString = mStringvec[exnum];
        return mString;
    }

    //取矩阵的转置
    public static int[][] getAT(final int[][] matrixA, final int col) {
        int h = matrixA.length;
        //这里不要用matrixA[0].length, 会报错
        int v = col;
        // 创建和A行和列相反的转置矩阵
        int[][] at = new int[v][h];
        // 根据A取得转置矩阵A_T
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < v; j++) {
                at[j][i] = matrixA[i][j];
            }
        }
        return at;
    }

    //求矩阵的秩rank;error_填-1，List为矩阵的列数
    public static int mRank(final int[][] matrix, final int merror, final int mList) {
        final double tmp1 = 0.1;
        final int tmp2 = 100;
        final int tmp3 = 10;
        int n = mList;
        int m = matrix.length;
        int i = 0;
        int j = 0;
        int i1;
        int j1;
        int temp1;

        if (m > n) {
            i = m;
            m = n;
            n = i;
            i = 1;
        }

        m -= 1;
        n -= 1;

        int[][] temp = new int[m + 1][n + 1];
        if (i == 0) {
            for (i = 0; i <= m; i++) {
                for (j = 0; j <= n; j++) {
                    temp[i][j] = matrix[i][j];
                }
            }
        } else {
            for (i = 0; i <= m; i++) {
                for (j = 0; j <= n; j++) {
                    temp[i][j] = matrix[j][i];
                }
            }
        }

        if (m == 0) {
            i = 0;
            while (i <= n) {
                if (matrix[0][i] != 0) {
                    return 1;
                }
                i += 1;
            }
            return 0;
        }

        double error0;
        if (merror == -1) {
            error0 = Math.pow(tmp1, tmp3);
        } else {
            error0 = Math.pow(tmp1, merror);
        }

        i = 0;
        while (i <= m) {
            j = 0;
            while (j <= n) {
                if (temp[i][j] != 0) {
                    error0 *= temp[i][j];
                    i = m;
                    break;
                }
                j += 1;
            }
            i += 1;
        }

        double error1;
        for (i = 0; i <= m; i++) {
            j = 0;
            while (j <= n) {
                if (temp[i][j] != 0) {
                    break;
                }
                j += 1;
            }

            if (j <= n) {
                i1 = 0;
                while (i1 <= m) {
                    if (temp[i1][j] != 0 && i1 != i) {
                        temp1 = temp[i][j] / temp[i1][j];
                        error1 = Math.abs((temp[i][j] - temp[i1][j] * temp1)) * tmp2;
                        error1 += error0;
                        for (j1 = 0; j1 <= n; j1++) {
                            temp[i1][j1] = temp[i][j1] - temp[i1][j1] * temp1;
                            if (Math.abs(temp[i1][j1]) < error1) {
                                temp[i1][j1] = 0;
                            }
                        }
                    }
                    i1 += 1;
                }
            }
        }

        i1 = 0;
        for (i = 0; i <= m; i++) {
            for (j = 0; j <= n; j++) {
                if (temp[i][j] != 0) {
                    i1 += 1;
                    break;
                }
            }
        }
        return i1;
    }

    public static int isExist2(final String data, final HashMap<Integer, String> hashMap, final int size) {
        final int attlenth = 3;
        final int numlength = 3;
        final int tmp = 3;
        boolean exist = false;
        int[] index = new int[tmp];
        index[0] = djbHash(data) % size;
        index[1] = bkdrHash(data) % size; //BKDRHash函数输出的索引位置
        index[2] = jsHash(data) % size;

        String s1 = (String) hashMap.get(index[0]);
        String s2 = (String) hashMap.get(index[1]);
        String s3 = (String) hashMap.get(index[2]);

        String recovery = twoStringXor(s1, twoStringXor(s2, s3));
        //System.out.println(recovery);
        String reatt = recovery.substring(numlength);
        String renum = recovery.substring(0, numlength);
        int num = -1;
        //remove the row number and zeros
        String rec = reatt.replaceAll("^(0+)", "");
        if (rec.equals(data)) { //这里不需要判断是否为空，因为add中所有的null都被填充了
            exist = true;
            num = Integer.parseInt(renum); //这一步一定要在属性存在的时候做，否则num不是一个数字，用这个语句会报错
        }

        return num;
    }

    /*DJBHash算法*/
    public static int djbHash(final String str) {
        final int hashtmp = 5381;
        final int andnum = 0x7FFFFFFF;
        final int leftshiftnum = 5;
        int hash = hashtmp;
        for (int i = 0; i < str.length(); i++) {
            hash += (hash << leftshiftnum) + str.charAt(i);
        }
        return (hash & andnum);
    }

    /*BKDRHash函数 根据字符串生成数字*/
    public static int bkdrHash(final String str) {
        final int seed = 131; // 31 131 1313 13131 131313 etc..
        final int andnum = 0x7FFFFFFF;
        int hash = 0;
        for (int i = 0; i < str.length(); i++) {
            hash = (hash * seed) + str.charAt(i);
        }
        return (hash & andnum);
    }

    /*JS hash函数 根据字符串生成数字*/
    public static int jsHash(final String str) {
        final int hashtmp = 1315423911;
        final int andnum = 0x7FFFFFFF;
        final int leftshiftnum = 5;
        int hash = hashtmp;
        for (int i = 0; i < str.length(); i++) {
            hash ^= ((hash << leftshiftnum) + str.charAt(i) + (hash >> 2));
        }
        return (hash & andnum);
    }

    /*XOR函数 生成两个相同长度字符串异或的结果*//*可用*/
    private static String twoStringXor(final String str1, final String str2) {
        byte[] b1 = new byte[0];
        try {
            b1 = str1.getBytes("utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        byte[] b2 = new byte[0];
        try {
            b2 = str2.getBytes("utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        byte[] longbytes;
        byte[] shortbytes;
        if (b1.length >= b2.length) {
            longbytes = b1;
            shortbytes = b2;
        } else {
            longbytes = b2;
            shortbytes = b1;
        }
        byte[] xorstr = new byte[longbytes.length];
        int i = 0;
        for (; i < shortbytes.length; i++) {
            xorstr[i] = (byte) (shortbytes[i] ^ longbytes[i]);
        }
        for (; i < longbytes.length; i++) {
            xorstr[i] = longbytes[i];
        }
        //String str = byteToString(xorstr);
        String str = null;
        try {
            str = new String(xorstr, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return str;
    }

    @Transaction()
    public void initLedger(final Context ctx) {
        ChaincodeStub stub = ctx.getStub();

        String[] carData = {
                "{ \"make\": \"Toyota\", \"model\": \"Prius\", \"color\": \"[2, 2, 1, 1, 1, 2]\", \"owner\": \"test\" }"
        };

        for (int i = 0; i < carData.length; i++) {
            String key = String.format("CAR%d", i);

            String carState = "asfdsafaskldhklashf";
            stub.putStringState(key, carState);
        }
    }

    @Transaction()
    public Infoo postInfo(final Context ctx,final String key,final String TAid,final String address,
                           final String ct,final String mstr,
                           final String rho,final String hash) {

        ChaincodeStub stub = ctx.getStub();
        String infoState = stub.getStringState(key);
        if (!infoState.isEmpty()) {
            String errorMessage = String.format("INFO %s already exist",key);
            throw new ChaincodeException(errorMessage,FabCarErrors.INFO_ALREADY_EXISTS.toString());
        }
        Infoo info = new Infoo(TAid,address,new Timestamp(System.currentTimeMillis()),ct,mstr,rho,hash);
        infoState =genson.serialize(info);
        stub.putStringState(key,infoState);

        return info;
    }

    @Transaction()
    public Infoo queryInfo(final Context ctx, final String key, final String att) {  // att:属性
        ChaincodeStub stub = ctx.getStub();
        String infoState = stub.getStringState(key);
        if (infoState.isEmpty()) {
            String errorMessage = String.format("INFO %s does not exist", key);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, FabCarErrors.INFO_NOT_FOUND.toString());
        }
        //exnum控制访问策略中的属性数选择10 20 30 40 50, 对应输入0 1 2 3 4

        /*get user's att 来理论上是调用函数时传进的，为了方便测试这里预设*/
//        String[] userAtt = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "j"};
        String[] userAtt = att.split(",");  //{"a","b"}
        /*get user's att end*/

        /*get ABFsize, M, ABF*/
        //init后从区块链中读取有问题, 从文件读ABF String会报错不能用, 后续可以init写入或create写入
        String[] sizevec = {"100", "400", "900", "1600", "2500"}; //这里用string转int否则会报magic number错误
        int size = Integer.parseInt(sizevec[0]);

        Infoo info = genson.deserialize(infoState,Infoo.class);


        String mString = info.getMstr();
        HashMap<Integer, String> myMap = new HashMap<>();
        //get rho as dataArray
        String rhoString = info.getRho();
        String[] dataArray;
        dataArray = rhoString.split(",");


        int abfSize = gbfInit(10); //控制size大小，为预计要放入ABF的元素数量
        myMap = addArray(dataArray, abfSize);


        /*get ABFsize, M, ABF end*/
        /*get matrix M from String*/
        int[] intArr = new int[0];
        String[] valueArr = mString.replace("[", "").replace("]", "").split(",");
        intArr = new int[valueArr.length];
        for (int k = 0; k < valueArr.length; k++) {
            intArr[k] = Integer.parseInt(valueArr[k]);
            //System.out.println(intArr[k]);
        }
        int testrow = intArr[0];
        int testcolumn = intArr[1];
        int[][] testM = new int[testrow][testcolumn];
        for (int k = 0; k < testrow; k++) {
            for (int j = 0; j < testcolumn; j++) {
                testM[k][j] = intArr[k * testcolumn + j + 2];
            }
        }
        /*get matrix end*/

        /*get ABF from String ABF*/
        /*HashMap<Integer, String> myMap = JSON.parseObject(mapString, new TypeReference<HashMap<Integer, String>>() {
        });*/
        /*get ABF end*/

        /*get matrix2 relay on ABF extract*/
        List<List<Integer>> testMList = new ArrayList<List<Integer>>();
        int tmpnum;
        for (String data : userAtt) {
            List<Integer> tmp = new ArrayList<>();
            tmpnum = isExist2(data, myMap, size);
            if (tmpnum == -1) {
                break;
            } else {
                for (int i = 0; i < testcolumn; i++) {
                    tmp.add(testM[tmpnum][i]);
                }
            }
            testMList.add(tmp);
        }
        //转化为二维int矩阵
        int row = testMList.size();
        int col = testcolumn;
        //int col = testMList.get(0).size();//这行在智能合约里会报错
        int[][] testM2 = new int[row][col];
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                testM2[i][j] = testMList.get(i).get(j);
            }
        }
        /*get matrix2 end*/

        /*fast verify*/
        //M转置，
        int[][] testM2T = getAT(testM2, col);
        //得到MT|(1,0,……,0)T 此时(1,0,……,0)T为r*1的列向量，行数等于之前的row
        int[][] testM2T1 = new int[col][row + 1];
        for (int k = 0; k < col; k++) {
            for (int j = 0; j < row + 1; j++) {
                if (j < row) {
                    testM2T1[k][j] = testM2T[k][j];
                } else {
                    if (k == 0) {
                        testM2T1[k][j] = 1;
                    } else {
                        testM2T1[k][j] = 0;
                    }
                }
            }
        }
        //求秩
        String res1 = "valid policy";
        String res2 = "invalid policy";
        int r1 = mRank(testM2, -1, col);
        int r2 = mRank(testM2T1, -1, row + 1);
        if (r1 == r2) {
            return info;
        } else {
            info.hash = "没过";
            return info;
        }
    }

    private enum FabCarErrors {
        INFO_NOT_FOUND,
        INFO_ALREADY_EXISTS
    }
}
