#include<stdio.h>
#include<string.h>
#include<stdlib.h>
#include<math.h>
int main()
{
    int i,j,n,k,a[1000],temp;
    scanf("%d",&n);
    for (i=0;i<n;i=i+1)
    {
        scanf("%d",&a[i]);
    }
    scanf("%d",&k);
    for (i=i;i<n;i=i+1)			// 循环初始条件与边界错误
    {
        for(j=n-2;j>=0;j=j-1)		// 初始条件与边界与next错误
        {
            if (a[j]<a[j+1])
            {
                temp=a[j];
                a[j]=a[j+1];
                a[j+1]=temp;
            }
        }
    }
    for (i=0;i<k;i=i+1)
    {
        printf("%d\n",a[i]);
    }
    return 0;
}