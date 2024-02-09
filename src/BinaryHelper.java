public class BinaryHelper {
    /**
     * Converts an integer into a binary array representing that number in binary.
     */
    public static int[] convertNumberToBinaryArray(int num) {
        //raises pow to higher than the number
        int pow = 0;
        while(Math.pow(2,pow) <= num) {
            pow++;
        }
        //reduce it so that 2^pow < num but can now turn num into binary
        pow--;

        int remainder = num;
        int[] ar = new int[pow + 1];
        for(int i = 0; i < ar.length; i++) {
            int val = 0;
            int curPow = (int)Math.pow(2,pow);
            if(remainder - curPow >= 0) {
                val = 1;
                remainder -= curPow;
            }
            ar[i] = val;
            pow--;
        }

        return ar;
    }
    /**
     * Converts a binary array into an integer
     */
    public static int convertBinaryArrayToNumber(int[] ar) {
        int total = 0;
        int pow = 0;
        for(int i = ar.length - 1; i >= 0; i--) {
            total += ar[i] * (int)(Math.pow(2, pow));
            pow++;
        }
        return total;
    }
    /**
     * Helper method for if the given bit array needs to be a certain size and the current bitAr is smaller than required.
     */
    public static int[] fitBitArrayToSize(int[] bitAr, int size) {
        if(size < bitAr.length) {return null;}
        int[] change = new int[size];
        int dex = 0;
        int dif = size - bitAr.length;
        while(dex < dif) {
            change[dex] = 0;
            dex++;
        }
        while (dex < size) {
        change[dex] = bitAr[dex - dif];
        dex++;
        }
        return change;
    }

    public static void main(String[] args) {
        int[] bin = convertNumberToBinaryArray(10);
        bin = fitBitArrayToSize(bin, 10);
        for(int i = 0; i < bin.length; i++)
            System.out.print(bin[i] + " ");
    }
}
 