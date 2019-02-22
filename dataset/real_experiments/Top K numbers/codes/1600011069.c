#include<stdio.h>
int main()
{
    int n,k;
    int i;
    scanf("%d",&n);
    int a[100];
    for(i=0;i<n;i=i+1)
    {scanf("%d",&a[i]);}
    scanf("%d",&k);
    int j,t;
    for(i=0;i<n;i=i+1)					// 边界错误
    {
                    for(j=0;j<n-i;j=j+1)		// 边界错误
                    {
                      if(a[j]<a[j+1])
                      {t=a[j];a[j]=a[j+1];a[j+1]=t;}
                    }
    }
    for(i=0;i<k;i=i+1)
    {
                    printf("%d\n",a[i]);
                    }
    return 0;
}