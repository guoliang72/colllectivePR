#include <stdio.h>
#include <stdlib.h>
#include <string.h>
int main()
{  int a[1000],n,k,i,temp;
   scanf("%d",&n);
   for(i=0;i<n;i=i+1)
   {
    scanf("%d",&a[i]);
    }
  scanf("%d",&k);
   int j;
   for(i=0;i<n-1;i=i+1)
   {
        for(j=0;j<n-i-1;j=j+1)
        {    
             if(a[j]>a[j+1])			// 比较方向错误
             {
                            temp=a[j];
                            a[j]=a[j+1];
                            a[j+1]=temp;
             }
        }
   }
   for(i=0;i<k;i=i+1)
   printf("%d\n",a[i]);
                            
	
   return 0;
}